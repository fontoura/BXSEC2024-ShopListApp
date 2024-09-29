package main

import (
	"crypto/tls"
	"fmt"
	"net/http"

	_ "embed"
)

var (
	//go:embed server.crt
	serverCrt []byte

	//go:embed server.key
	serverKey []byte
)

func main() {
	EnsureDb()

	SetupRoutes()

	// Create TLS configuration with the embedded certs
	cert, err := tls.X509KeyPair(serverCrt, serverKey)
	if err != nil {
		fmt.Printf("Error loading key pair: %s\n", err)
		return
	}

	tlsConfig := &tls.Config{
		Certificates: []tls.Certificate{cert},
	}

	server := &http.Server{
		Addr:      ":8443",
		TLSConfig: tlsConfig,
	}

	fmt.Println("Server is listening on port 8443...")
	if err := server.ListenAndServeTLS("", ""); err != nil {
		fmt.Printf("Error starting server: %s\n", err)
	}
}
