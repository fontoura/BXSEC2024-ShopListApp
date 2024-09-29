package main

import (
	"database/sql"
	"encoding/hex"
	"fmt"
	"log"
	"math/rand"
	"time"

	"crypto/md5"
	"io"
)

// User structure to hold user data
type User struct {
	Id           int
	Username     string
	PasswordHash string
	Salt         string
	IsSuperUser  bool
}

// GetUser gets an user from the database
func GetUserByUsername(db *sql.DB, username string) (*User, error) {
	var id, is_superuser int
	var passwordHash, salt string
	err := db.QueryRow("SELECT id, password_hash, salt, is_superuser FROM user WHERE username = ?", username).Scan(&id, &passwordHash, &salt, &is_superuser)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		log.Println(err)
		return nil, err
	}

	return &User{id, username, passwordHash, salt, is_superuser != 0}, nil
}

// AuthenticateUser authenticates a user with username and password
func AuthenticateUser(db *sql.DB, loggedUsername, username, password string) (*User, error) {
	loggedUser, err := GetUserByUsername(db, loggedUsername)
	if err != nil {
		log.Println(err)
		return nil, err
	}
	if loggedUser != nil {
		log.Printf("Logged user: %v, isSuperUser: %v\n", loggedUser.Username, loggedUser.IsSuperUser)
	}

	user, err := GetUserByUsername(db, username)
	if err != nil {
		log.Println(err)
		return nil, err
	}

	if user == nil {
		log.Printf("User %s not found.\n", username)
		return nil, nil
	}

	time.Sleep(100 * time.Millisecond)
	if !(loggedUser != nil && loggedUser.IsSuperUser) {
		// Validate password
		hashedPassword := hashPassword(password, user.Salt)
		if hashedPassword != user.PasswordHash {
			log.Printf("Invalid password for user %s.\n", username)
			return nil, nil
		}
	}

	log.Printf("User %s authenticated successfully.\n", username)
	return user, nil
}

// EnrollUser creates a new user with a hashed password and random salt
func EnrollUser(db *sql.DB, username, password string, isSuperUser bool) (*User, error) {
	var id int
	salt := generateSalt(16)
	hashedPassword := hashPassword(password, salt)
	log.Printf("Enrolling user %s with password %s, salt %s and hash %s.\n", username, password, salt, hashedPassword)
	superuserFlag := 0
	if isSuperUser {
		superuserFlag = 1
	}

	err := db.QueryRow(
		"INSERT INTO user (username, password_hash, salt, is_superuser) VALUES (?, ?, ?, ?) RETURNING ID",
		username, hashedPassword, salt, superuserFlag,
	).Scan(&id)

	if err != nil {
		if IsFailedUniqueConstraintError(err) {
			err = NewConflictError(fmt.Sprintf("User %s already exists", username), err)
		}
		return nil, err
	}

	log.Printf("User %s enrolled with ID %d.\n", username, id)
	return &User{id, username, hashedPassword, salt, isSuperUser}, nil
}

// GenerateSalt generates a random salt of a specified length
func generateSalt(length int) string {
	const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	salt := make([]byte, length)
	for i := range salt {
		salt[i] = charset[rand.Intn(len(charset))]
	}
	return string(salt)
}

// HashPassword generates a bcrypt hash of the password with the given salt
func hashPassword(password, salt string) string {
	h := md5.New()
	io.WriteString(h, password)
	io.WriteString(h, salt)
	hashedPassword := h.Sum(nil)
	return hex.EncodeToString(hashedPassword)
}
