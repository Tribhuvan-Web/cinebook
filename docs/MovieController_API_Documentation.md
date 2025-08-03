# MovieController - Complete API Documentation

This document provides comprehensive documentation for the MovieController in the MovieDekho application, covering movie
management operations for both administrators and regular users.

## Base URL

Assuming the application runs on localhost:8080, the base URL is:

```
http://localhost:8080/api/movies
```

---

## 🎬 MOVIE MANAGEMENT ENDPOINTS

### 1. Add New Movie (Admin Only)

**Endpoint:** `POST /add`  
**Authentication:** Required (Admin Role)

Adds a new movie to the system. Only accessible by administrators.

```bash
curl -X POST http://localhost:8080/api/movies/add \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Avengers: Endgame",
    "description": "The epic conclusion to the Infinity Saga that the universe has been building towards for twenty-two films.",
    "duration": 181,
    "language": "English",
    "releaseDate": "2019-04-26",
    "genre": "Action, Drama, Adventure",
    "rating": 8.4,
    "cast": "Robert Downey Jr., Chris Evans, Mark Ruffalo, Chris Hemsworth",
    "director": "Anthony Russo, Joe Russo",
    "posterUrl": "https://example.com/posters/avengers-endgame.jpg",
    "trailerUrl": "https://example.com/trailers/avengers-endgame.mp4"
  }'
```

### 2. Get All Movies

**Endpoint:** `GET /all`

Retrieves all movies from the system. Accessible to all users.

```bash
curl -X GET http://localhost:8080/api/movies/all \
  -H "Accept: application/json"
```

### 3. Get Movie by ID

**Endpoint:** `GET /{id}`

Retrieves a specific movie by its ID.

```bash
# Get movie with ID 1
curl -X GET http://localhost:8080/api/movies/1 \
  -H "Accept: application/json"

# Get movie with ID 5
curl -X GET http://localhost:8080/api/movies/5 \
  -H "Accept: application/json"
```

### 4. Update Movie (Admin Only)

**Endpoint:** `PUT /{id}`  
**Authentication:** Required (Admin Role)

Updates an existing movie. Only accessible by administrators.

```bash
curl -X PUT http://localhost:8080/api/movies/1 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Avengers: Endgame - Director'\''s Cut",
    "description": "The epic conclusion to the Infinity Saga with additional scenes and director commentary.",
    "duration": 195,
    "language": "English",
    "releaseDate": "2019-04-26",
    "genre": "Action, Drama, Adventure",
    "rating": 8.6,
    "cast": "Robert Downey Jr., Chris Evans, Mark Ruffalo, Chris Hemsworth",
    "director": "Anthony Russo, Joe Russo",
    "posterUrl": "https://example.com/posters/avengers-endgame-directors-cut.jpg",
    "trailerUrl": "https://example.com/trailers/avengers-endgame-directors-cut.mp4"
  }'
```

### 5. Delete Movie (Admin Only)

**Endpoint:** `DELETE /{id}`  
**Authentication:** Required (Admin Role)

Removes a movie from the system. Only accessible by administrators.

```bash
curl -X DELETE http://localhost:8080/api/movies/1 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### 6. Search Movies

**Endpoint:** `GET /search`

Searches for movies based on various criteria.

```bash
# Search by title
curl -X GET "http://localhost:8080/api/movies/search?title=Avengers" \
  -H "Accept: application/json"

# Search by genre
curl -X GET "http://localhost:8080/api/movies/search?genre=Action" \
  -H "Accept: application/json"

# Search by language
curl -X GET "http://localhost:8080/api/movies/search?language=English" \
  -H "Accept: application/json"

# Search by director
curl -X GET "http://localhost:8080/api/movies/search?director=Christopher%20Nolan" \
  -H "Accept: application/json"

# Search by cast member
curl -X GET "http://localhost:8080/api/movies/search?cast=Robert%20Downey%20Jr" \
  -H "Accept: application/json"

# Combined search
curl -X GET "http://localhost:8080/api/movies/search?title=Dark&language=English&genre=Action" \
  -H "Accept: application/json"
```

### 7. Filter Movies by Genre

**Endpoint:** `GET /genre/{genre}`  
**Authentication:** Optional

Filters movies by specific genre.

```bash
# Get all Action movies
curl -X GET http://localhost:8080/api/movies/genre/Action \
  -H "Accept: application/json"

# Get all Comedy movies
curl -X GET http://localhost:8080/api/movies/genre/Comedy \
  -H "Accept: application/json"

# Get all Drama movies
curl -X GET http://localhost:8080/api/movies/genre/Drama \
  -H "Accept: application/json"
```

### 8. Filter Movies by Language

**Endpoint:** `GET /language/{language}`  
**Authentication:** Optional

Filters movies by specific language.

```bash
# Get all English movies
curl -X GET http://localhost:8080/api/movies/language/English \
  -H "Accept: application/json"

# Get all Hindi movies
curl -X GET http://localhost:8080/api/movies/language/Hindi \
  -H "Accept: application/json"

# Get all Spanish movies
curl -X GET http://localhost:8080/api/movies/language/Spanish \
  -H "Accept: application/json"
```

### 9. Get Movies by Rating Range

**Endpoint:** `GET /rating-range`  
**Authentication:** Optional

Retrieves movies within a specific rating range.

```bash
# Get movies with rating between 8.0 and 10.0
curl -X GET "http://localhost:8080/api/movies/rating-range?minRating=8.0&maxRating=10.0" \
  -H "Accept: application/json"

# Get movies with rating between 7.0 and 8.5
curl -X GET "http://localhost:8080/api/movies/rating-range?minRating=7.0&maxRating=8.5" \
  -H "Accept: application/json"

# Get high-rated movies (9.0+)
curl -X GET "http://localhost:8080/api/movies/rating-range?minRating=9.0&maxRating=10.0" \
  -H "Accept: application/json"
```

---

## 🎭 REAL-WORLD SCENARIOS

### Scenario 1: Admin Adding Multiple Movies

```bash
# Add first movie - Action
curl -X POST http://localhost:8080/api/movies/add \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "John Wick: Chapter 4",
    "description": "John Wick uncovers a path to defeating The High Table.",
    "duration": 169,
    "language": "English",
    "releaseDate": "2023-03-24",
    "genre": "Action, Crime, Thriller",
    "rating": 7.7,
    "cast": "Keanu Reeves, Donnie Yen, Bill Skarsgård",
    "director": "Chad Stahelski",
    "posterUrl": "https://example.com/posters/john-wick-4.jpg",
    "trailerUrl": "https://example.com/trailers/john-wick-4.mp4"
  }'

# Add second movie - Comedy
curl -X POST http://localhost:8080/api/movies/add \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Guardians of the Galaxy Vol. 3",
    "description": "Peter Quill must rally his team around him to defend the universe.",
    "duration": 150,
    "language": "English",
    "releaseDate": "2023-05-05",
    "genre": "Action, Adventure, Comedy",
    "rating": 7.9,
    "cast": "Chris Pratt, Zoe Saldana, Dave Bautista",
    "director": "James Gunn",
    "posterUrl": "https://example.com/posters/gotg3.jpg",
    "trailerUrl": "https://example.com/trailers/gotg3.mp4"
  }'

# Add third movie - Horror
curl -X POST http://localhost:8080/api/movies/add \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Scream VI",
    "description": "The survivors of the Ghostface killings leave Woodsboro behind.",
    "duration": 123,
    "language": "English",
    "releaseDate": "2023-03-10",
    "genre": "Horror, Mystery, Thriller",
    "rating": 6.5,
    "cast": "Melissa Barrera, Jenna Ortega, Jasmin Savoy Brown",
    "director": "Matt Bettinelli-Olpin, Tyler Gillett",
    "posterUrl": "https://example.com/posters/scream6.jpg",
    "trailerUrl": "https://example.com/trailers/scream6.mp4"
  }'
```

### Scenario 2: User Browsing and Searching Movies

```bash
# User views all available movies
curl -X GET http://localhost:8080/api/movies/all \
  -H "Accept: application/json"

# User searches for action movies
curl -X GET "http://localhost:8080/api/movies/search?genre=Action" \
  -H "Accept: application/json"

# User looks for highly-rated movies
curl -X GET "http://localhost:8080/api/movies/rating-range?minRating=8.0&maxRating=10.0" \
  -H "Accept: application/json"

# User searches for movies with specific actor
curl -X GET "http://localhost:8080/api/movies/search?cast=Keanu%20Reeves" \
  -H "Accept: application/json"

# User gets details of a specific movie
curl -X GET http://localhost:8080/api/movies/1 \
  -H "Accept: application/json"
```

### Scenario 3: Admin Managing Movie Catalog

```bash
# Get all movies to review catalog
curl -X GET http://localhost:8080/api/movies/all \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Accept: application/json"

# Update movie information (e.g., rating after reviews)
curl -X PUT http://localhost:8080/api/movies/1 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "John Wick: Chapter 4",
    "description": "John Wick uncovers a path to defeating The High Table. Updated with director commentary.",
    "duration": 169,
    "language": "English",
    "releaseDate": "2023-03-24",
    "genre": "Action, Crime, Thriller",
    "rating": 8.1,
    "cast": "Keanu Reeves, Donnie Yen, Bill Skarsgård",
    "director": "Chad Stahelski",
    "posterUrl": "https://example.com/posters/john-wick-4-updated.jpg",
    "trailerUrl": "https://example.com/trailers/john-wick-4-extended.mp4"
  }'

# Remove outdated movie
curl -X DELETE http://localhost:8080/api/movies/10 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Scenario 4: Advanced Search and Filtering

```bash
# Find English action movies by Christopher Nolan
curl -X GET "http://localhost:8080/api/movies/search?language=English&genre=Action&director=Christopher%20Nolan" \
  -H "Accept: application/json"

# Get all movies in a specific language
curl -X GET http://localhost:8080/api/movies/language/Hindi \
  -H "Accept: application/json"

# Filter by multiple genres
curl -X GET http://localhost:8080/api/movies/genre/Comedy \
  -H "Accept: application/json"

# Find movies within specific duration range (using rating-range as example)
curl -X GET "http://localhost:8080/api/movies/rating-range?minRating=7.5&maxRating=9.0" \
  -H "Accept: application/json"
```

---

## 🎯 KEY FEATURES

### Movie Management:

- ✅ **CRUD Operations** - Complete create, read, update, delete functionality
- ✅ **Admin Controls** - Secure admin-only movie management
- ✅ **Rich Metadata** - Comprehensive movie information storage
- ✅ **Media URLs** - Support for poster and trailer links

### Search & Discovery:

- ✅ **Multi-criteria Search** - Search by title, genre, language, director, cast
- ✅ **Genre Filtering** - Filter movies by specific genres
- ✅ **Language Filtering** - Filter movies by language
- ✅ **Rating Range** - Find movies within specific rating ranges
- ✅ **Combined Filters** - Use multiple search criteria simultaneously

### User Experience:

- ✅ **Public Access** - Browse movies without authentication
- ✅ **Detailed Information** - Complete movie metadata
- ✅ **Flexible Search** - Multiple ways to discover content
- ✅ **Real-time Updates** - Admin changes reflected immediately

### Data Management:

- ✅ **Validation** - Input validation for movie data
- ✅ **Error Handling** - Comprehensive error responses
- ✅ **Timestamps** - Creation and update tracking
- ✅ **Unique Identification** - Auto-generated movie IDs

---

## 📝 IMPORTANT NOTES

1. **Authentication:** Admin operations require valid JWT token with admin role
2. **Validation:** All movie data is validated before storage
3. **Search Performance:** Consider implementing pagination for large datasets
4. **Image URLs:** Ensure poster and trailer URLs are accessible
5. **Rating Scale:** Movie ratings should be between 0.0 and 10.0
6. **Date Format:** Use ISO date format (YYYY-MM-DD) for release dates
7. **Duration:** Movie duration is stored in minutes
8. **Genre Format:** Multiple genres can be comma-separated
9. **Cast Format:** Multiple cast members can be comma-separated
10. **Security:** Always validate admin permissions for modification operations

---

### Movie Booking Integration:

```bash
# Typical flow: Browse -> Select -> Book
# 1. User browses movies
curl -X GET http://localhost:8080/api/movies/all

# 2. User selects a movie
curl -X GET http://localhost:8080/api/movies/1

# 3. System can use movie ID for slot management
curl -X GET http://localhost:8080/api/movie-slots/movie/1
```

---

## 🚀 GETTING STARTED

1. **Browse Movies:** Use GET /all to see available movies
2. **Search:** Use search endpoints to find specific movies
3. **Get Details:** Use GET /{id} for complete movie information
4. **Admin Setup:** Ensure admin authentication for management operations
5. **Integration:** Use movie IDs for slot and booking management
