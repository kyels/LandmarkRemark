# Landmark Remark

## Outline
### Accounts (Implicit)
I implemented user accounts using the Google Firebase Authentication API.
This was an implicit requirement due to the need to share remarks across user
accounts.
I used the UI library for this service to reduce time spent on UI design.
### Current Location on Map (User Story 1)
Implemented with the Google Map API, familiar UI to user with blue dot
showing current location.
Permission to access location is requested from the user as required by
API.
### Landmark Pins (User Story 2)
* This app is primarily focused on looking at notes about landmarks.
I chose to integrate the google places API such that most notes
would have a logical connection to an existing landmark.
* When a user hits the floating action button, the places picker opens at
there current location. They can use this current location as a lat/lng
position or choose/search a nearby landmark; They are then directed to
write and post their remark.
### Displaying Remarks (User Story 3 & 4)
* When a user opens the app, it will present a set number of pins
 representing the landmarks with the most recent remarks. When a user
 clicks on one of these pins, they are shown the most recent remark.
 They can then click through to a list of remarks for that landmark.
* For a user to see their own remarks, they tap a button in the toolbar
that refreshes the view with pins to all the landmarks they have remarked.
### Database and Search (User Story 5)
* I used the Firebase Database API and attempted to have a database that
was primarily de-normalised; allowing rapid response for geo coordinates
while also allowing lists of remarks per user and per landmark available.
This was another implicit requirement as this database could grow quite large.
* I decided to integrate Firebase Database with Algolia as it had more
capabilities for search.
This required setting up a Node.js script on Heroku to sync the data
between the services.

## Time allocation
* UI 4 hours (Google Maps, other UI elements)
* Location 3 hours (Places Picker, Google Location API)
* Database 3 hours (Firebase Database)
* Search 2 hours (Algolia Search index receiving updates from Firebase
Database via a Node.js script running on Heroku)

## Known Issues and Limitations
* Unfortunately I wasn't able to finish the search implementation. After
a few trial and error attempts with some other implementations and
finally settling on Algolia I ran out of time to implement the UI and
display results.
* The app loads all pins currently on the map, this would be an easy fix
with a bit more work on setting the filters for the database call.
* The my places button has not been implemented but the database is
setup to make this data retrieval simple.
* The extension to show all comments of a landmark has not been
implemented. Currently just showing the most recent remark.
* Android Studio was updated to 3.0 after my initial submission.
This required some changes to gradle build files to fix build issues.
### Testing
I've been using the Android Studio emulator for testing,
running a Nexus 5 with API 21.
My solution requires Google Play Services to be installed.