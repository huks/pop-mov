Popular Movies App
========

This is the stage 1 of the popular movies app for the Udacity's Android Developer Nanodegree program.(https://www.udacity.com/course/android-developer-nanodegree--nd801).

### themoviedb.org API Key is required.

In order for the app to function properly, an API key for themoviedb.org must be included with the build.

Please include the unique key for the build by replacing the following line in [USER_HOME]/app/build.gradle

`it.buildConfigField 'String', 'TMDB_API_KEY', 'MY_API_KEY_HERE'`