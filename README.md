# Match - Football Watch Party Finder ⚽️

Match is a mobile Android application designed to connect football fans. Whether you're looking to host a viewing party for the next big game or want to join fellow fans at a local venue, Match brings the stadium atmosphere to your neighborhood. 

## Key Features

*   **Secure Registration & Personalized Feed:** To create an account, users must provide their full name, email, and password. During this process, they are required to select at least one favorite team (either a local Israeli team, a National team, or both). This selection generates a customized upcoming match schedule on their main feed.
*   **Host Watch Parties:** Users can create new events, specifying the match, location, venue type (e.g., home, pub), and maximum capacity.
*   **Request & Approval System:** To ensure host control, joining an event sends a "Pending" request to the host.
*   **Notification Center:** Hosts receive requests from other users and can gracefully Approve them. Approved users are automatically added to the guest list.
*   **Real-Time Updates:** Seamless synchronization of party capacities and guest lists using Firebase Cloud Firestore.

## Tech Stack & Architecture

*   **Language:** Kotlin
*   **Architecture:** MVC / Android native architecture
*   **Backend / Database:** Firebase Authentication (User login/registration), Firebase Cloud Firestore (NoSQL database for users, parties, and requests).
*   **Networking:** [Retrofit2](https://square.github.io/retrofit/) for handling REST API requests to fetch live football data.
*   **Asynchronous Programming:** Kotlin Coroutines & Lifecycle Scopes for smooth background processing.

## Important Note regarding the API (API-Football)

This application uses the [API-Football](https://www.api-football.com/) free tier to fetch match data. 
Please note that the free tier is subject to certain limitations:
1. **Rate Limit:** The API is strictly limited to **100 requests per day**. If the daily limit is exceeded, the upcoming matches list will appear empty, and the API will return a "Too Many Requests" error in the logs. **This is an expected behavior due to the API limitation, not a bug in the code.**
2. **Data Availability:** To ensure consistent demonstration data on the free tier, the application displays historical match fixtures from the **2023 season**.

*(If you wish to fully test the app and the daily limit has been reached, I have provided a backup API key: . Just replace the `myApiKey` variable in `MainActivity.kt` inside the `fetchUpcomingMatches` function).*

## Developer
Developed by **Noy Ben-Mucha** ([@noybm12](https://github.com/noybm12))
Software Engineering Student.
