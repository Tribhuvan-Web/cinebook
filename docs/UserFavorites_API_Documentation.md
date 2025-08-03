# User Favorites Feature - Simple API Documentation

## Overview

The User Favorites feature allows authenticated users to add movies to their personal favorites list, remove them, and view their favorite movies. This feature uses simple path variables and returns string messages for easy integration.

## 🎯 KEY FEATURES

### Favorites Management:
- ✅ **Add to Favorites** - Users can add movies using path variable `/favorites/{movieId}`
- ✅ **Remove from Favorites** - Users can remove movies using DELETE `/favorites/{movieId}`
- ✅ **View Favorites** - Users can view their complete favorites list as movie array
- ✅ **Check Favorite Status** - Users can check if a specific movie is in their favorites
- ✅ **Simple Responses** - Returns simple string messages instead of complex DTOs

### Data Persistence:
- ✅ **Many-to-Many Relationship** - Users can have multiple favorite movies
- ✅ **Junction Table** - `user_favorite_movies` table manages the relationship
- ✅ **Lazy Loading** - Favorites are loaded efficiently when needed

---

## 🔐 AUTHENTICATION

All favorite movie operations require user authentication:
- **Authorization Header**: `Bearer <JWT_TOKEN>`
- **Role Required**: `ROLE_USER`
- **Access**: Only authenticated users can manage their own favorites

---

## 📚 FAVORITES ENDPOINTS

### 1. Get User's Favorite Movies

**Endpoint:** `GET /api/user/favorites`  
**Authentication:** Required (User Role)

Retrieves all movies in the user's favorites list as an array.

```bash
curl -X GET http://localhost:8080/api/user/favorites \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Avengers: Endgame",
    "releaseDate": "2019-04-26",
    "duration": "181",
    "genre": "Action, Drama, Adventure",
    "description": "The epic conclusion to the Infinity Saga...",
    "trailer": "https://example.com/trailers/avengers-endgame.mp4",
    "certification": "PG-13",
    "thumbnail": "https://example.com/posters/avengers-endgame.jpg",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31"
  },
  {
    "id": 3,
    "title": "The Dark Knight",
    "releaseDate": "2008-07-18",
    "duration": "152",
    "genre": "Action, Crime, Drama",
    "description": "Batman battles the Joker...",
    "trailer": "https://example.com/trailers/dark-knight.mp4",
    "certification": "PG-13",
    "thumbnail": "https://example.com/posters/dark-knight.jpg"
  }
]
```

### 2. Add Movie to Favorites

**Endpoint:** `POST /api/user/favorites/{movieId}`  
**Authentication:** Required (User Role)

Adds a movie to the user's favorites list using path variable.

```bash
curl -X POST http://localhost:8080/api/user/favorites/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```
"Movie added to favorites successfully"
```

**Error Responses:**
- `404 Not Found`: "Movie not found with ID: 1"
- `200 OK`: "Movie is already in your favorites" (if already added)

### 3. Remove Movie from Favorites

**Endpoint:** `DELETE /api/user/favorites/{movieId}`  
**Authentication:** Required (User Role)

Removes a specific movie from the user's favorites list.

```bash
curl -X DELETE http://localhost:8080/api/user/favorites/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```
"Movie removed from favorites successfully"
```

**Error Responses:**
- `200 OK`: "Movie was not in your favorites" (if not in favorites)

### 4. Check if Movie is in Favorites

**Endpoint:** `GET /api/user/favorites/check/{movieId}`  
**Authentication:** Required (User Role)

Checks if a specific movie is in the user's favorites list.

```bash
curl -X GET http://localhost:8080/api/user/favorites/check/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```
"Movie is in favorites"
```
or
```
"Movie is not in favorites"
```

### 5. User Dashboard (Enhanced with Favorites)

**Endpoint:** `GET /api/user/dashboard`  
**Authentication:** Required (User Role)

Retrieves user dashboard information including favorites data.

```bash
curl -X GET http://localhost:8080/api/user/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

**Response:**
```json
{
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "phone": "+1234567890",
    "gender": "Male",
    "role": "ROLE_USER"
  },
  "favoriteMoviesCount": 5,
  "recentFavorites": [
    {
      "id": 1,
      "title": "Avengers: Endgame",
      "releaseDate": "2019-04-26",
      "duration": "181",
      "genre": "Action, Drama, Adventure"
    },
    {
      "id": 3,
      "title": "The Dark Knight",
      "releaseDate": "2008-07-18",
      "duration": "152",
      "genre": "Action, Crime, Drama"
    }
  ]
}
```

---

## 🎭 REAL-WORLD SCENARIOS

### Scenario 1: User Building Their Favorites Collection

```bash
# User browses movies and finds one they like
curl -X GET http://localhost:8080/api/movies/1

# User adds movie to favorites (using path variable)
curl -X POST http://localhost:8080/api/user/favorites/1 \
  -H "Authorization: Bearer USER_JWT_TOKEN"

# User adds another movie to favorites
curl -X POST http://localhost:8080/api/user/favorites/3 \
  -H "Authorization: Bearer USER_JWT_TOKEN"

# User views their favorites list
curl -X GET http://localhost:8080/api/user/favorites \
  -H "Authorization: Bearer USER_JWT_TOKEN"
```

### Scenario 2: User Managing Favorites from Movie Details Page

```bash
# User views a movie and wants to check if it's in favorites
curl -X GET http://localhost:8080/api/user/favorites/check/1 \
  -H "Authorization: Bearer USER_JWT_TOKEN"

# If not in favorites, user adds it (simple path variable)
curl -X POST http://localhost:8080/api/user/favorites/1 \
  -H "Authorization: Bearer USER_JWT_TOKEN"

# Later, user decides to remove it
curl -X DELETE http://localhost:8080/api/user/favorites/1 \
  -H "Authorization: Bearer USER_JWT_TOKEN"
```

---

## 📋 SIMPLIFIED API DESIGN

### No DTOs Required:
- **Add to Favorites**: `POST /api/user/favorites/{movieId}` - No request body needed
- **Remove from Favorites**: `DELETE /api/user/favorites/{movieId}` - Path variable only
- **Check Status**: Returns simple string messages instead of JSON objects
- **Get Favorites**: Returns direct array of `MovieResponseDTO` objects

### Simple String Responses:
```
✅ "Movie added to favorites successfully"
✅ "Movie removed from favorites successfully"  
✅ "Movie is already in your favorites"
✅ "Movie was not in your favorites"
✅ "Movie is in favorites"
✅ "Movie is not in favorites"
```

---

## 🛠️ DATABASE SCHEMA

### Junction Table: user_favorite_movies
```sql
CREATE TABLE user_favorite_movies (
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES available_movie(id) ON DELETE CASCADE
);
```

---

## 📝 IMPORTANT NOTES

1. **Simple Design**: No DTOs required - uses path variables and simple strings
2. **Path Variables**: Movie ID is passed in URL path instead of request body
3. **String Messages**: Returns descriptive string messages instead of complex JSON
4. **Authentication**: All operations require valid JWT token with user role
5. **Data Integrity**: Cascade deletes ensure data consistency
6. **User Isolation**: Users can only manage their own favorites
7. **Performance**: Lazy loading optimizes database queries

---

## 🔄 INTEGRATION EXAMPLES

### Frontend JavaScript Example:
```javascript
// Add to favorites - simple POST with movie ID in path
const addToFavorites = async (movieId) => {
  const response = await fetch(`/api/user/favorites/${movieId}`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${userToken}`
    }
  });
  const message = await response.text(); // Returns simple string
  return message;
};

// Remove from favorites
const removeFromFavorites = async (movieId) => {
  const response = await fetch(`/api/user/favorites/${movieId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${userToken}`
    }
  });
  const message = await response.text();
  return message;
};

// Check if movie is favorite
const checkFavoriteStatus = async (movieId) => {
  const response = await fetch(`/api/user/favorites/check/${movieId}`, {
    headers: {
      'Authorization': `Bearer ${userToken}`
    }
  });
  const message = await response.text();
  return message.includes("is in favorites");
};

// Get all favorites
const getFavorites = async () => {
  const response = await fetch('/api/user/favorites', {
    headers: {
      'Authorization': `Bearer ${userToken}`
    }
  });
  return response.json(); // Returns array of movies
};
```

---

## 🚀 GETTING STARTED

1. **User Authentication**: Ensure user has valid JWT token
2. **Add Movie**: `POST /api/user/favorites/{movieId}` 
3. **Remove Movie**: `DELETE /api/user/favorites/{movieId}`
4. **Check Status**: `GET /api/user/favorites/check/{movieId}`
5. **View All**: `GET /api/user/favorites`
6. **Dashboard**: `GET /api/user/dashboard`

This simplified design makes the favorites feature easy to integrate and maintain!
