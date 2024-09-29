package main

import (
	"fmt"
	"net/http"
	"strings"

	jwt "github.com/golang-jwt/jwt/v4"

	_ "embed"
)

type UserChecker func(string) bool
type RequestHandler func(http.ResponseWriter, *http.Request, string)

type HttpMux struct {
	Get    http.HandlerFunc
	Post   http.HandlerFunc
	Put    http.HandlerFunc
	Delete http.HandlerFunc
}

func WithMux(mux HttpMux) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodGet:
			if mux.Get != nil {
				mux.Get(w, r)
				return
			}
		case http.MethodPost:
			if mux.Post != nil {
				mux.Post(w, r)
				return
			}
		case http.MethodPut:
			if mux.Put != nil {
				mux.Put(w, r)
				return
			}
		case http.MethodDelete:
			if mux.Delete != nil {
				mux.Delete(w, r)
				return
			}
		default:
			break
		}
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
	}
}

func WithTokenValidation(jwtKey []byte, userChecker UserChecker, next RequestHandler) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		authHeader := r.Header.Get("Authorization")
		if authHeader == "" {
			http.Error(w, "No token provided", http.StatusUnauthorized)
			return
		}

		tokenString := strings.TrimPrefix(authHeader, "Bearer ")
		claims := &Claims{}

		token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
			if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
				return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
			}
			return jwtKey, nil
		})

		if err != nil {
			http.Error(w, "Invalid token", http.StatusUnauthorized)
			return
		}

		if !token.Valid {
			http.Error(w, "Invalid token", http.StatusUnauthorized)
			return
		}

		username := claims.Username
		if !userChecker(username) {
			http.Error(w, "Invalid user", http.StatusUnauthorized)
			return
		}

		next(w, r, username)
	}
}
