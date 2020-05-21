# android-sdk

## SDK to integrate with Silt's signup
This is an SDK to integrate with an Android's app Silt's KYC & ID verification.
The SDK framework files are inside `siltsdk` directory.

## How to
You can check the only code you will need to add to your app in the example ViewController file:
app/src/main/java/com/silt/siltdemojava/MainActivity.java
You can import the SDK from the jitpack: https://jitpack.io/#getsilt/android-sdk

## How it works
This SDK consists only in a button that opens a webview and captures once the user has finished the verification in the webview a user ID.
This is the user ID you can use to verify the ID verification of that user in Silt.
