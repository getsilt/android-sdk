# android-sdk

## SDK to integrate with Silt's signup
This is an SDK to integrate with an Android's app Silt's KYC & ID verification.
The SDK framework files are inside `siltsdk` directory.

## How to

## How it works
This SDK consists only in a button that opens a webview and captures once the user has finished the verification in the webview a user ID.
This is the user ID you can use to verify the ID verification of that user in Silt.

## How to integrate
You can check the an example of integration in: app/src/main/java/com/silt/siltdemojava

### 1 Add the repo from Jitpack
You can import the SDK from the jitpack: https://jitpack.io/#getsilt/android-sdk

1. Add the maven repo of nitpick in the **Project’s build.gradle File** 
```allprojects {
    repositories {
        …
        maven { url "https://jitpack.io" }
    }
}
```
2. Add the Silt SDK dependency in your **App’s build.gradle**

```
dependencies {
   …
    implementation 'com.github.getsilt:android-sdk:X.X.X’
}

```

### 2 Add a button
1. Add the predefined Silt buttons with blue or silver backgrounds (you can modify them),  or the trademark and a button of your style.

**Blue Silt button**
```
<Button
    android:id="@+id/silt_button"
    style="@style/SiltButtonBlue"
    android:onClick="loadSiltSignUp"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"></Button>
```
Or
**Silver Silt button**
```
<Button
    android:id="@+id/silt_button"
    style="@style/SiltButton"
    android:onClick="loadSiltSignUp"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"></Button>
```
Or
Any button of yours  **and Trademark**
```
<!-- Any button you want -->
<Button
    android:id="@+id/silt_button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:background="@drawable/silt_button_silver"
    android:bottomLeftRadius="10dp"
    android:bottomRightRadius="10dp"
    android:drawablePadding="10dp"
    android:onClick="loadSiltSignUp"
    android:paddingLeft="20dip"
    android:paddingTop="10dip"
    android:paddingRight="20dip"
    android:paddingBottom="10dip"
    android:text="@string/verify_custom_button"
    android:textAllCaps="false"
    android:textSize="18sp"
    android:topLeftRadius="10dp"
    android:topRightRadius="10dp"
    />

<!-- Trademark: Use powered by under your custom button -->
<LinearLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:gravity="center"
    android:orientation="vertical">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:drawableEnd="@drawable/silt_blue_small"
        android:drawableRight="@drawable/silt_blue_small"
        android:drawablePadding="10dp"
        android:padding="10dp"
        android:text="@string/silt_powered_by"></TextView>
</LinearLayout>

```

2. Create a listener for the function the button is going to call  (only if you are not using onClick Button attribute)

```
binding.loginLayoutIncluded.siltButton.setOnClickListener {
      this.loadSiltSignUp()
    }
```

### 3 Add the function that starts Silt webview and it’s handler

#### Kotlin
1. Add the property VERIFY_CODE to the class 
```
private val VERIFY_CODE = 777
```

2. Add the function that opens the webview activity
```
private fun loadSiltSignUp() {
  // ask for your companyAppId on hello@getsilt.com
  // and use it in the initializer as extra
  // siltActivity.putExtra("companyAppId", "{YOUR_CUSTOMER_APP_ID}")
  // demo companyAppId: 9f936bc0-328f-4985-95b1-2c562061711f
  val siltActivity = Intent(getActivity(), SiltActivity::class.java)
  siltActivity.putExtra("companyAppId", "6f7838e4-3b30-447e-81c1-3e123bc34980")
  startActivityForResult(siltActivity, this.VERIFY_CODE)
}
```

3. Add the function that handles the webview response
```
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
  super.onActivityResult(requestCode, resultCode, data)
  if (resultCode == Activity.RESULT_OK && requestCode == this.VERIFY_CODE) {
    if (data!!.hasExtra("silt_user_id") && data.hasExtra("company_app_token")) {
      Log.d(
        "Verify",
        "####### Got user Id from Silt: " + data.getStringExtra("silt_user_id")
      )
      Log.d(
        "Verify",
        "####### Got Company App Token: " + data.getStringExtra("company_app_token")
      )
      /*
              * 1. Place here a function that calls to your backend with user_id
              * 2. Your backend should make a request to Silt API to /v1/users/{user_id}
              * 3. Retrieve the info that you want from that response
              * */
    }
  }
}
```
————————————————————
#### Java
1. Add the property VERIFY_CODE to the class
```
private static final int VERIFY_CODE = 777;
```

2. Add the function that opens the webview activity
```
public void loadSiltSignUp(View v) {
    // Ask for your companyAppId on hello@getsilt.com
    // or get it from https://dashboard.getsilt.com
    // and use it in the initializer as extra
    // siltActivity.putExtra("companyAppId", "{YOUR_CUSTOMER_APP_ID}")
    // demo companyAppId: 2022a022-a662-4c58-8865-a1fb904d2cde
    // If you want to use other services, like biocheck, add the path to the propper
    // siltActivity.putExtra("path", "biocheck");
    // Keep in mind that to use biocheck, you will need to create a company app temporary token
    // through API. Check more about this in getsilt.com/developers
    // siltActivity.putExtra("extraQuery", "&temp_token=1462a0c1-ab62-8888-7824-b9fd115c1acd");
    Intent siltActivity = new Intent(this, SiltActivity.class);
    siltActivity.putExtra("companyAppId", "2022a022-a662-4c58-8865-a1fb904d2cde");
    siltActivity.putExtra("extraQuery", "&user_email=test@getsilt.com");
    startActivityForResult(siltActivity, VERIFY_CODE);
}
```
3. Add the function that handles the webview response
```
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == VERIFY_CODE) {
        if (data.hasExtra("silt_user_id") && data.hasExtra("company_app_token")) {
            Log.d(TAG, "####### Got user Id from Silt: " + data.getStringExtra("silt_user_id"));
            Log.d(TAG, "####### Got Company App Token: " + data.getStringExtra("company_app_token"));
            /*
            * 1. Place here a function that calls to your backend with user_id
            * 2. Your backend should make a request to Silt API to /v1/users/{user_id}
            * 3. Retrieve the info that you want from that response
            * */

        }
    }
}


```

### 4 Make the call to your backend to check user’s status

With the parameters `data.getStringExtra("silt_user_id")` and `data.getStringExtra("company_app_token")`  call your backend that will call Silt’s Backend to check the verification status and info of that user.
