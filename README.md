# Boolder Android

Boolder is the best way to discover bouldering in Fontainebleau.

The app is available at https://www.boolder.com/en/app

More info: https://www.boolder.com/en/about

## Contribute

Want to help us improve the app for thousands of climbers? Great!

Here are a few ways you can contribute:
- Open an issue if you find a bug
- Open an issue if you want to suggest an improvement
- Open a Pull Request (please get in touch with us beforehand, though)

We already have a lot of features waiting to be built, and lots of new ideas to try out!
We'd be happy to share the fun with you :)

As the project is still young, the best way to get started is to drop us a line at hello@boolder.com

You can also contribute to our mapping efforts at https://www.boolder.com/en/contribute

## Build the app

### Codebase overview

The app is written in Kotlin, and uses several libraries:
- Ktor 
- KotlinX.Serialization 
- Room 
- Mapbox SDK 
- Koin 
- Coil

Here is a 15-min video describing the architecture of the app : https://youtu.be/Qk7gX1CaMk4

### Mapbox setup

Create a file named `gradle.properties` in the `~/.gradle/` directory, with the following content:

```
MAPBOX_ACCESS_TOKEN={ACCESS_TOKEN}
MAPBOX_DOWNLOADS_TOKEN={DOWNLOADS_TOKEN}
```

Replace respectively `{ACCESS_TOKEN}` and `{DOWNLOADS_TOKEN}` with the mapbox default public token
and the secret download key.

More info [here](https://docs.mapbox.com/help/troubleshooting/private-access-token-android-and-ios/).

