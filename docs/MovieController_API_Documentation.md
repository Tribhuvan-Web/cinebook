# MovieController - Complete API Documentation

This document provides comprehensive documentation for the MovieController in the MovieDekho application, covering movie management operations for both administrators and regular users.

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
**Authentication:** Optional

Retrieves all movies from the system. Accessible to all users.

```bash
curl -X GET http://localhost:8080/api/movies/all \
  -H "Accept: application/json"
```

### 3. Get Movie by ID
**Endpoint:** `GET /{id}`  
**Authentication:** Optional

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
**Authentication:** Optional

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

## 📊 EXAMPLE RESPONSES

### Successful Movie Addition
```json
{
  "id": 1,
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
  "trailerUrl": "https://example.com/trailers/avengers-endgame.mp4",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### All Movies Response
```json
[
  {
    "id": 1,
    "title": "Avengers: Endgame",
    "description": "The epic conclusion to the Infinity Saga...",
    "duration": 181,
    "language": "English",
    "releaseDate": "2019-04-26",
    "genre": "Action, Drama, Adventure",
    "rating": 8.4,
    "cast": "Robert Downey Jr., Chris Evans, Mark Ruffalo, Chris Hemsworth",
    "director": "Anthony Russo, Joe Russo",
    "posterUrl": "https://example.com/posters/avengers-endgame.jpg",
    "trailerUrl": "https://example.com/trailers/avengers-endgame.mp4"
  },
  {
    "id": 2,
    "title": "The Dark Knight",
    "description": "When the menace known as the Joker wreaks havoc...",
    "duration": 152,
    "language": "English",
    "releaseDate": "2008-07-18",
    "genre": "Action, Crime, Drama",
    "rating": 9.0,
    "cast": "Christian Bale, Heath Ledger, Aaron Eckhart",
    "director": "Christopher Nolan",
    "posterUrl": "https://example.com/posters/dark-knight.jpg",
    "trailerUrl": "https://example.com/trailers/dark-knight.mp4"
  }
]
```

### Single Movie Response
```json
{
  "id": 1,
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
  "trailerUrl": "https://example.com/trailers/avengers-endgame.mp4",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Search Results Response
```json
[
  {
    "id": 1,
    "title": "Avengers: Endgame",
    "description": "The epic conclusion to the Infinity Saga...",
    "duration": 181,
    "language": "English",
    "releaseDate": "2019-04-26",
    "genre": "Action, Drama, Adventure",
    "rating": 8.4,
    "cast": "Robert Downey Jr., Chris Evans, Mark Ruffalo, Chris Hemsworth",
    "director": "Anthony Russo, Joe Russo"
  },
  {
    "id": 3,
    "title": "Avengers: Infinity War",
    "description": "The Avengers and their allies must be willing to sacrifice all...",
    "duration": 149,
    "language": "English",
    "releaseDate": "2018-04-27",
    "genre": "Action, Adventure, Drama",
    "rating": 8.4,
    "cast": "Robert Downey Jr., Chris Hemsworth, Mark Ruffalo",
    "director": "Anthony Russo, Joe Russo"
  }
]
```

### Error Responses

**404 - Movie Not Found:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Movie not found with id: 999",
  "path": "/api/movies/999"
}
```

**403 - Forbidden (Admin Only Endpoints):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Admin role required.",
  "path": "/api/movies/add"
}
```

**400 - Bad Request:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/movies/add",
  "validationErrors": [
    "Title is required",
    "Duration must be positive",
    "Rating must be between 0 and 10"
  ]
}
```

---

## 📋 DATA TRANSFER OBJECTS (DTOs)

### MovieDto (Request/Response)
```json
{
  "title": "Movie Title",
  "description": "Detailed movie description",
  "duration": 120,
  "language": "English",
  "releaseDate": "2024-01-15",
  "genre": "Action, Drama",
  "rating": 8.5,
  "cast": "Actor 1, Actor 2, Actor 3",
  "director": "Director Name",
  "posterUrl": "https://example.com/poster.jpg",
  "trailerUrl": "https://example.com/trailer.mp4"
}
```

### Movie Entity (Full Response)
```json
{
  "id": 1,
  "title": "Movie Title",
  "description": "Detailed movie description",
  "duration": 120,
  "language": "English",
  "releaseDate": "2024-01-15",
  "genre": "Action, Drama",
  "rating": 8.5,
  "cast": "Actor 1, Actor 2, Actor 3",
  "director": "Director Name",
  "posterUrl": "https://example.com/poster.jpg",
  "trailerUrl": "https://example.com/trailer.mp4",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
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

## 🔧 TESTING DIFFERENT SCENARIOS

### Test Movie Validation

```bash
# Test adding movie with missing required fields
curl -X POST http://localhost:8080/api/movies/add \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "",
    "description": "Test movie without title"
  }'

# Test adding movie with invalid rating
curl -X POST http://localhost:8080/api/movies/add \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Movie",
    "description": "Test description",
    "duration": 120,
    "language": "English",
    "releaseDate": "2024-01-15",
    "genre": "Action",
    "rating": 15.0,
    "cast": "Test Actor",
    "director": "Test Director"
  }'

# Test adding movie with negative duration
curl -X POST http://localhost:8080/api/movies/add \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Invalid Duration Movie",
    "description": "Movie with negative duration",
    "duration": -10,
    "language": "English",
    "releaseDate": "2024-01-15",
    "genre": "Action",
    "rating": 7.5,
    "cast": "Test Actor",
    "director": "Test Director"
  }'
```

### Test Access Control

```bash
# Test admin operations without authentication
curl -X POST http://localhost:8080/api/movies/add \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Unauthorized Movie",
    "description": "Should fail without auth"
  }'

# Test admin operations with user token (should fail)
curl -X POST http://localhost:8080/api/movies/add \
  -H "Authorization: Bearer USER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "User Trying Admin Action",
    "description": "Should fail with user token"
  }'

# Test updating non-existent movie
curl -X PUT http://localhost:8080/api/movies/999999 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Non-existent Movie Update",
    "description": "Should return 404"
  }'

# Test deleting non-existent movie
curl -X DELETE http://localhost:8080/api/movies/999999 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Test Search Edge Cases

```bash
# Test search with empty parameters
curl -X GET "http://localhost:8080/api/movies/search?" \
  -H "Accept: application/json"

# Test search with non-existent criteria
curl -X GET "http://localhost:8080/api/movies/search?title=NonExistentMovie" \
  -H "Accept: application/json"

# Test rating range with invalid parameters
curl -X GET "http://localhost:8080/api/movies/rating-range?minRating=10&maxRating=5" \
  -H "Accept: application/json"

# Test genre filter with non-existent genre
curl -X GET http://localhost:8080/api/movies/genre/NonExistentGenre \
  -H "Accept: application/json"

# Test language filter with non-existent language
curl -X GET http://localhost:8080/api/movies/language/NonExistentLanguage \
  -H "Accept: application/json"
```

---

## 💻 PowerShell Examples (For Windows Users)

```powershell
# Add movie using PowerShell
$adminToken = "YOUR_ADMIN_JWT_TOKEN"
$movieData = @{
    title = "Spider-Man: No Way Home"
    description = "Peter Parker seeks help from Doctor Strange when his identity is revealed."
    duration = 148
    language = "English"
    releaseDate = "2021-12-17"
    genre = "Action, Adventure, Fantasy"
    rating = 8.2
    cast = "Tom Holland, Zendaya, Benedict Cumberbatch"
    director = "Jon Watts"
    posterUrl = "https://example.com/posters/spiderman-nwh.jpg"
    trailerUrl = "https://example.com/trailers/spiderman-nwh.mp4"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/movies/add" `
  -Method POST `
  -Headers @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
  } `
  -Body $movieData

# Get all movies
$movies = Invoke-RestMethod -Uri "http://localhost:8080/api/movies/all" `
  -Method GET `
  -Headers @{"Accept" = "application/json"}

Write-Output "Total movies: $($movies.Count)"

# Search for action movies
$actionMovies = Invoke-RestMethod -Uri "http://localhost:8080/api/movies/search?genre=Action" `
  -Method GET `
  -Headers @{"Accept" = "application/json"}

Write-Output "Action movies found: $($actionMovies.Count)"

# Get high-rated movies
$highRatedMovies = Invoke-RestMethod -Uri "http://localhost:8080/api/movies/rating-range?minRating=8.0&maxRating=10.0" `
  -Method GET `
  -Headers @{"Accept" = "application/json"}

foreach ($movie in $highRatedMovies) {
    Write-Output "$($movie.title) - Rating: $($movie.rating)"
}

# Update movie
$updatedMovieData = @{
    title = "Spider-Man: No Way Home - Extended Edition"
    description = "Peter Parker seeks help from Doctor Strange when his identity is revealed. Extended version with deleted scenes."
    duration = 165
    language = "English"
    releaseDate = "2021-12-17"
    genre = "Action, Adventure, Fantasy"
    rating = 8.4
    cast = "Tom Holland, Zendaya, Benedict Cumberbatch"
    director = "Jon Watts"
    posterUrl = "https://example.com/posters/spiderman-nwh-extended.jpg"
    trailerUrl = "https://example.com/trailers/spiderman-nwh-extended.mp4"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/movies/1" `
  -Method PUT `
  -Headers @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
  } `
  -Body $updatedMovieData

# Delete movie
Invoke-RestMethod -Uri "http://localhost:8080/api/movies/1" `
  -Method DELETE `
  -Headers @{"Authorization" = "Bearer $adminToken"}
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

## 🔄 INTEGRATION PATTERNS

### Frontend Integration:
```javascript
// Movie browsing functionality
const getMovies = async () => {
  const response = await fetch('/api/movies/all');
  return await response.json();
};

const searchMovies = async (searchParams) => {
  const queryString = new URLSearchParams(searchParams).toString();
  const response = await fetch(`/api/movies/search?${queryString}`);
  return await response.json();
};

const getMovieDetails = async (movieId) => {
  const response = await fetch(`/api/movies/${movieId}`);
  return await response.json();
};

// Admin functionality
const addMovie = async (movieData, adminToken) => {
  const response = await fetch('/api/movies/add', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(movieData)
  });
  return await response.json();
};

const updateMovie = async (movieId, movieData, adminToken) => {
  const response = await fetch(`/api/movies/${movieId}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(movieData)
  });
  return await response.json();
};

const deleteMovie = async (movieId, adminToken) => {
  await fetch(`/api/movies/${movieId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${adminToken}`
    }
  });
};
```

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
