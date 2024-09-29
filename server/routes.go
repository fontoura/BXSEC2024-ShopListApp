package main

import (
	"bytes"
	"encoding/json"
	"io"
	"log"
	"net/http"
	"strconv"
	"time"

	jwt "github.com/golang-jwt/jwt/v4"

	_ "embed"
)

// Secret key for signing tokens
var jwtKey = []byte("2d292ab9-466b-4487-997a-6ba4145d7498")

// JWT claims struct
type Claims struct {
	Username string `json:"username"`
	jwt.RegisteredClaims
}

type UserDto struct {
	Username    string  `json:"username"`
	Password    *string `json:"password"`
	IsSuperUser bool    `json:"isSuperUser"`
}

type ShopListEntryDto struct {
	Id     *int   `json:"id"`
	Name   string `json:"name"`
	Amount int    `json:"amount"`
}

var systemUserName = "SYSTEM"

func SetupRoutes() {
	http.HandleFunc("/users", WithMux(HttpMux{
		Post: WithTokenValidation(jwtKey, systemUserChecker, handlerToEnrollUser),
	}))
	http.HandleFunc("/users/login", WithMux(HttpMux{
		Post: WithTokenValidation(jwtKey, defaultUserChecker, handlerToCreateToken),
	}))

	http.HandleFunc("/entries", WithMux(HttpMux{
		Get:  WithTokenValidation(jwtKey, defaultUserChecker, handlerToGetShopListEntries),
		Post: WithTokenValidation(jwtKey, defaultUserChecker, handlerToAddShopListEntry),
	}))
	http.HandleFunc("/entries/{id}", WithMux(HttpMux{
		Get:    WithTokenValidation(jwtKey, defaultUserChecker, handlerToGetShopListEntry),
		Put:    WithTokenValidation(jwtKey, defaultUserChecker, handlerToEditShopListEntry),
		Delete: WithTokenValidation(jwtKey, defaultUserChecker, handlerToDeleteShopListEntry),
	}))
}

func systemUserChecker(username string) bool {
	return username == systemUserName
}

func defaultUserChecker(username string) bool {
	return true
}

func fallbackErrorHandler(err *HttpError, w http.ResponseWriter, _ *http.Request) {
	log.Printf("Error: %s\n", err.Message)
	http.Error(w, err.Message, err.HttpErrorCode)
}

func handlerToGetShopListEntries(w http.ResponseWriter, r *http.Request, username string) {
	log.Printf("Getting entries...\n")

	db, err := ConnectDb()
	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to connect to database", err),
			w, r)
		return
	}

	entries, err := LoadEntries(db, username)
	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to retrieve entries from database", err),
			w, r)
		return
	}

	entriesDto := make([]ShopListEntryDto, len(entries))
	for i, entry := range entries {
		entriesDto[i] = ShopListEntryDto{
			Id:     entry.Id,
			Name:   entry.Name,
			Amount: entry.Amount,
		}
	}

	w.Header().Set("Content-Type", "application/json")
	j, _ := json.Marshal(map[string]any{
		"entries": entriesDto,
	})
	w.Write(j)
}

func handlerToAddShopListEntry(w http.ResponseWriter, r *http.Request, username string) {
	log.Printf("Adding entry...\n")

	db, err := ConnectDb()
	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to connect to database", err),
			w, r)
		return
	}

	var entryDto ShopListEntryDto
	if err := json.NewDecoder(r.Body).Decode(&entryDto); err != nil {
		fallbackErrorHandler(
			NewBadRequestError("Failed to parse JSON", err),
			w, r)
		return
	}

	entry := ShopListEntry{
		Name:   entryDto.Name,
		Amount: entryDto.Amount,
	}

	if err := AddEntry(db, username, &entry); err != nil {
		fallbackErrorHandler(
			AsHttpError(err, "Failed to add entry to database"),
			w, r)
		http.Error(w, "Internal server error", http.StatusInternalServerError)
		return
	}

	entryDto.Id = entry.Id

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	j, _ := json.Marshal(entryDto)
	w.Write(j)
}

func handlerToGetShopListEntry(w http.ResponseWriter, r *http.Request, username string) {
	log.Printf("Getting entry...\n")

	db, err := ConnectDb()
	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to connect to database", err),
			w, r)
		return
	}

	id, err := strconv.Atoi(r.PathValue("id"))
	if err != nil {
		fallbackErrorHandler(
			NewBadRequestError("Failed to parse ID", err),
			w, r)
		return
	}

	entry, err := GetEntry(db, id)

	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to retrieve entry from database", err),
			w, r)
		return
	}
	if entry == nil {
		fallbackErrorHandler(
			NewNotFoundError("Entry not found", nil),
			w, r)
		return
	}

	entryDto := ShopListEntryDto{
		Id:     &id,
		Name:   entry.Name,
		Amount: entry.Amount,
	}

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	j, _ := json.Marshal(entryDto)
	w.Write(j)
}

func handlerToEditShopListEntry(w http.ResponseWriter, r *http.Request, username string) {
	log.Printf("Editing entry...\n")

	db, err := ConnectDb()
	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to connect to database", err),
			w, r)
		return
	}

	body, err := io.ReadAll(r.Body)
	if err != nil {
		fallbackErrorHandler(
			NewBadRequestError("Failed to read request body", err),
			w, r)
		return
	}
	log.Printf("Raw request body: %s", string(body))

	var entryDto ShopListEntryDto
	if err := json.NewDecoder(bytes.NewReader(body)).Decode(&entryDto); err != nil {
		fallbackErrorHandler(
			NewBadRequestError("Failed to parse JSON", err),
			w, r)
		return
	}

	id, err := strconv.Atoi(r.PathValue("id"))
	if err != nil {
		fallbackErrorHandler(
			NewBadRequestError("Failed to parse ID", err),
			w, r)
		return
	}

	entry := ShopListEntry{
		Id:     &id,
		Name:   entryDto.Name,
		Amount: entryDto.Amount,
	}

	updated, err := UpdateEntry(db, &entry)
	if err != nil {
		fallbackErrorHandler(
			AsHttpError(err, "Failed to update entry in database"),
			w, r)
		return
	}
	if !updated {
		fallbackErrorHandler(
			NewNotFoundError("Entry not found", nil),
			w, r)
		return
	}

	entryDto.Id = entry.Id

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	j, _ := json.Marshal(entryDto)
	w.Write(j)
}

func handlerToDeleteShopListEntry(w http.ResponseWriter, r *http.Request, username string) {
	log.Printf("Deleting entry...\n")

	db, err := ConnectDb()
	if err != nil {
		log.Println(err)
		http.Error(w, "Internal server error", http.StatusInternalServerError)
		return
	}

	id, err := strconv.Atoi(r.PathValue("id"))
	if err != nil {
		fallbackErrorHandler(
			NewBadRequestError("Failed to parse ID", err),
			w, r)
		return
	}

	deleted, err := DeleteEntry(db, id)
	if err != nil {
		fallbackErrorHandler(
			AsHttpError(err, "Failed to delete entry from database"),
			w, r)
		log.Println(err)
		http.Error(w, "Internal server error", http.StatusInternalServerError)
		return
	}
	if !deleted {
		fallbackErrorHandler(
			NewNotFoundError("Entry not found", nil),
			w, r)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func handlerToEnrollUser(w http.ResponseWriter, r *http.Request, _ string) {
	log.Printf("Enrolling user...\n")

	db, err := ConnectDb()
	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to connect to database", err),
			w, r)
		return
	}

	var userDto UserDto
	if err := json.NewDecoder(r.Body).Decode(&userDto); err != nil {
		fallbackErrorHandler(
			NewBadRequestError("Failed to parse JSON", err),
			w, r)
		return
	}
	if userDto.Password == nil {
		fallbackErrorHandler(
			NewBadRequestError("Password must be provided", nil),
			w, r)
		return
	}

	_, err = EnrollUser(db, userDto.Username, *userDto.Password, userDto.IsSuperUser)
	if err != nil {
		fallbackErrorHandler(
			AsHttpError(err, "Failed to enroll user"),
			w, r)
		return
	}

	userDto.Password = nil
	w.WriteHeader(http.StatusCreated)
	j, _ := json.Marshal(userDto)
	w.Write(j)
}

func handlerToCreateToken(w http.ResponseWriter, r *http.Request, username string) {
	log.Printf("Authenticating user...\n")

	db, err := ConnectDb()
	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to connect to database", err),
			w, r)

		return
	}

	var userDto UserDto
	if err := json.NewDecoder(r.Body).Decode(&userDto); err != nil {
		fallbackErrorHandler(
			NewBadRequestError("Failed to parse JSON", err),
			w, r)
		return
	}
	if userDto.Password == nil {
		fallbackErrorHandler(
			NewBadRequestError("Password must be provided", nil),
			w, r)
		return
	}

	user, err := AuthenticateUser(db, username, userDto.Username, *userDto.Password)
	if err != nil {
		fallbackErrorHandler(
			AsHttpError(err, "Failed to authenticate user"),
			w, r)
		return
	}

	if user == nil {
		fallbackErrorHandler(
			NewUnauthorizedError("Invalid credentials", nil),
			w, r)
		return
	}

	claims := &Claims{
		Username: user.Username,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject:   user.Username,
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(30 * time.Minute)),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString(jwtKey)
	if err != nil {
		fallbackErrorHandler(
			NewInternalServerError("Failed to sign token", err),
			w, r)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	j, _ := json.Marshal(map[string]any{
		"token":       tokenString,
		"isSuperUser": user.IsSuperUser,
	})
	w.Write(j)
}
