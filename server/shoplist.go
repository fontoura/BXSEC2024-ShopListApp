package main

import (
	"database/sql"
	"errors"
	"log"
)

type ShopListEntry struct {
	Id     *int
	UserId *int
	Name   string
	Amount int
}

// Adds a new entry to the shop list.
// This method updates the ID in the provided entry.
func AddEntry(db *sql.DB, username string, entry *ShopListEntry) error {
	if db == nil {
		return errors.New("the database connection must be provided")
	}
	if entry == nil {
		return errors.New("the entry must be provided")
	}

	return db.QueryRow(
		"INSERT INTO shoplist_entry (user_id, name, amount) VALUES ((SELECT id FROM user WHERE username = ?), ?, ?) RETURNING id, user_id",
		username, entry.Name, entry.Amount,
	).Scan(&entry.Id, &entry.UserId)
}

// Gets an entry from the shop list by ID.
func GetEntry(db *sql.DB, id int) (*ShopListEntry, error) {
	if db == nil {
		return nil, errors.New("the database connection must be provided")
	}

	entry := &ShopListEntry{}
	row := db.QueryRow(
		"SELECT id, user_id, name, amount FROM shoplist_entry WHERE id = ?",
		id,
	)

	err := row.Scan(&entry.Id, &entry.UserId, &entry.Name, &entry.Amount)
	if err == sql.ErrNoRows {
		return nil, nil
	}

	if err != nil {
		entry = nil
	}
	return entry, nil
}

// Updates an entry in the shop list.
func UpdateEntry(db *sql.DB, entry *ShopListEntry) (bool, error) {
	if db == nil {
		return false, errors.New("the database connection must be provided")
	}
	if entry == nil {
		return false, errors.New("the entry must be provided")
	}
	if entry.Id == nil {
		return false, errors.New("the entry ID must be provided")
	}

	row := db.QueryRow(
		"UPDATE shoplist_entry SET name = ?, amount = ? WHERE id = ? RETURNING id, user_id",
		entry.Name, entry.Amount, entry.Id,
	)

	err := row.Scan(&entry.Id, &entry.UserId)
	if err == sql.ErrNoRows {
		return false, nil
	}
	if err != nil {
		return false, err
	}
	return true, nil
}

// Deletes an entry from the shop list.
func DeleteEntry(db *sql.DB, id int) (bool, error) {
	if db == nil {
		return false, errors.New("the database connection must be provided")
	}

	result, err := db.Exec("DELETE FROM shoplist_entry WHERE id = ?", id)
	if err != nil {
		return false, err
	}

	rowsAffected, err := result.RowsAffected()
	if err != nil {
		return false, err
	}

	return rowsAffected > 0, nil
}

// Loads all entries from the shop list for a given user.
func LoadEntries(db *sql.DB, username string) ([]ShopListEntry, error) {
	rows, err := db.Query(
		"SELECT id, name, amount FROM shoplist_entry WHERE user_id = (SELECT id FROM user WHERE username = ?)",
		username,
	)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	entries := []ShopListEntry{}
	for rows.Next() {
		entry := ShopListEntry{}
		err := rows.Scan(&entry.Id, &entry.Name, &entry.Amount)
		if err != nil {
			log.Println(err)
			continue
		}
		entries = append(entries, entry)
	}

	return entries, nil
}
