# SyncStage Android Quick Start Example

## Prerequisites

Before starting this guide make sure you have these prerequisites in place.

1. You have received a SyncStage access token by filling in the Early Access Request form here: <LINK>

2. We assume that you’re using [Android Studio](https://developer.android.com/studio). To test your Android device over wifi (as opposed to USB) you’ll need at least the “Bumble Bee” version of Android Studio. This is especially useful when you do not have a mini jack port in your smartphone and would like to use USB C headphones while debugging.

3. We host core SyncStage packages on Github. You’ll need a Github account, and will need to generate a personal access token that has the ‘read:packages’ scope enabled. For help creating a GitHub Personal Access Token please refer to [this guide](https://docs.github.com/en/enterprise-server@3.4/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token).

4. You’ll need to enable developer mode on your Android device so you can stream debug information back to Android Studio while testing. This is a useful guide for [enabling developer mode](https://www.samsung.com/uk/support/mobile-devices/how-do-i-turn-on-the-developer-options-menu-on-my-samsung-galaxy-device/#:~:text=1%20Go%20to%20%22Settings%22%2C,enable%20the%20Developer%20options%20menu.).

## Getting Started

First you’ll need to clone the SyncStage Android Example:

```
git clone https://github.com/opensesamemedia/syncstage-android/
cd syncstage-android
```

To run the example app, you’ll first need to make sure you can download the core SyncStage packages from GitHub. This requires that you add your GitHub username and personal access token (the one with ‘read:packages’ scope enabled) to ./build.gradle like this:

```
allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url "https://maven.pkg.github.com/opensesamemedia/syncstagesdkpackage"
            credentials {
                username = “YOUR GITHUB USERNAME”
                password = “YOUR GITHUB PERSONAL ACCESS TOKEN”
            }
        }
    }
}
```

Make sure your Android device is connected to Android Studio and then you hit ‘Run’. This will build the example application and then install it on your Android device.

### Permissions

SyncStage SDK requires following permissions to be added to AndroidManifest.xml:

```
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

## A note on user management in the current SyncStage platform

You are currently responsible for managing your own users in the SyncStage platform. Each access token allows you to host 7 simultaneous users on a single SyncStage server.

When connecting to the quick start app the users will be asked to select their user. If they choose clashing user numbers (between 0 and 6) their connections will fail.

We suggest that you explore the SDK and implement user management in your own service to make sure clashes don’t occur.


## Exploring the SyncStage test application

The SyncStage test application shows you basic UI representation of a typical SDK flow.

You must first copy and paste your Sync Stage early access token into the textbox at the top of the user interface.

![SyncStage Android Test Application](./images/android_app.png)

As you can see in the code, the access token is passed as a parameter when creating the SyncStage object.

```
fun initSDK() {
   sdk = SyncStage(
       accessToken = accessToken,
       userId = userId,
       ctx = applicationContext,
       onInitializedListener = {
           showToastFromNonUIThread("SDK Initialized Successfully")
       },
       onInitializationErrorListener = { _, msg -> showToastFromNonUIThread(msg) },
       onOperationErrorListener = { _, msg -> showToastFromNonUIThread(msg) },
       throwExceptionsOnErrors = false,
   )
   showToast("Initialization in progress...")
}
```

> If you're building your own application outside of the example, the developer token can also be passed directly into the code and asigned to the accessToken variable in the MainActivity class.

The first two buttons (‘Request `RECORD_AUDIO` permission’ and ‘Request `INTERNET` permission’) allow you to click to allow the permissions that Sync Stage requires from the Android Operating system.

After that you’ll see buttons for core SDK steps to establish a connection for a user. All of code for these steps can be found in use in the following file:

```
./app/src/main/java/media/opensesame/syncstagequickstart/MainActivity.kt
```

Those steps are:

1. `initSDK()` which creates the SDK object for future interactions. Importantly `initSDK()` accepts a userID variable which allows you to define the aforementioned user number on your SyncStage server.
2. `isInitialized()` which you can poll to check on the output of step 1. Once this returns `true` you can continue to the next steps.
3. `connect()` which uses your SyncStage Early Access token to connect to your SyncStage server. Importantly, once more than one user is connected, they will then be able to start communicating via SyncStage.
4. `disconnect()` which then disconnects the user that initialized this SDK instance from the server.

One noteworthy SDK function is:
5. `getExpirationTime()` which returns the amount of time you have left available on your SyncStage Early Access server token.

# Looking for assistance

You can lean on the SyncStage Early Access Slack channel for assistance with connecting with your applications. You can join the channel INSERT LINK HERE or send us an email via INSERT EMAIL HERE

# Recommendations

1. SyncStage SDK pipeline will work even faster with headphones plugged into your Android device as it helps our algorithm avoid certain calculations.

2. If you’re testing two smartphones in close proximity, you will need to be aware of feedback. While the SyncStage platform provides feedback cancellation, isolating the phones from each other in distance will ensure the best experience.

3. While it is possible to test with wireless headphones, we strongly recommend against it as doing so can add up to 300ms of latency.

