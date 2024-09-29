package main

import (
	"database/sql"
	"fmt"
	"log"

	sqliteDriver "github.com/glebarez/go-sqlite"
	sqliteLibrary "modernc.org/sqlite/lib"
)

var dbPath = "./sqlite.db"

func IsFailedUniqueConstraintError(err error) bool {
	sqliteErr, ok := err.(*sqliteDriver.Error)
	if ok {
		return sqliteErr.Code() == sqliteLibrary.SQLITE_CONSTRAINT_UNIQUE
	} else {
		return false
	}
}

// Connects to the SQLite database.
func ConnectDb() (*sql.DB, error) {
	// Connect to the SQLite database
	db, err := sql.Open("sqlite", dbPath)
	if err != nil {
		return nil, err
	}

	return db, nil
}

// Makes sure the database exists and it has the necessary tables.
func EnsureDb() {
	// Connect to the SQLite database
	db, err := sql.Open("sqlite", dbPath)
	if err != nil {
		log.Fatal(err)
	}
	defer db.Close()

	// SQL statement to create tables if they don't exist
	sqlStmt := `
	CREATE TABLE IF NOT EXISTS user (
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		username TEXT NOT NULL UNIQUE,
		password_hash TEXT NOT NULL,
		salt TEXT NOT NULL,
		is_superuser BOOLEAN NOT NULL
	);

	CREATE TABLE IF NOT EXISTS shoplist_entry (
		id INTEGER PRIMARY KEY AUTOINCREMENT,
		user_id INTEGER NOT NULL,
		name TEXT NOT NULL,
		amount INTEGER NOT NULL,
		FOREIGN KEY (user_id) REFERENCES user (id)
	);
	`

	// Execute the SQL statements to create tables
	_, err = db.Exec(sqlStmt)
	if err != nil {
		log.Fatalf("%q: %s\n", err, sqlStmt)
		return
	}

	fmt.Println("SQLite database tables created successfully.")
}
