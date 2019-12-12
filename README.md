# Coming Soon

[<img width="250" height="119" src="https://developer.mailchimp.com/documentation/mailchimp/img/lockup.svg"/>](http://www.mailchimp.com)

# Mailchimp Android SDK

Create and update your Mailchimp contacts from your Android app. The Mailchimp SDK allows you to,
* Add new users straight to your audience
* Update your contacts based on user action
* Add relevant tags to contacts
* Set or update merge fields

## Getting Started

### Requirements

* Android API Level 21 (5.0) and above

### Retrieving SDK Key

* TODO Update this when we finalize how to get the key

### Initializing the SDK

The first step is to create the configuration object. The configuration object has three different fields.

* SDK Key (Required): The SDK key gives you access to your audience. More details can be found in the previous section.
* Debug Mode (Optional): Debug Mode enables additional debug only functionality such as extra logging. This is off by default.
* Auto Tagging (Optional): Auto Tagging automatically tags contacts with information such as Device Type and Platform. This is on by default.

Below is an example of a potential configuration.

```kotlin
    val sdkKey = "12345678901234567890123456789012-us12" //Example SDK Key
    val isDebugBuild = BuildConfig.DEBUG
    val configuration = MailchimpSdkConfiguration.Builder(context, sdkKey)
        .isDebugModeEnabled(isDebugBuild)
        .isAutoTaggingEnabled(true)
        .build()
```

Once you have the configuration object setup, simply call the initialize method.

```kotlin
    val mailchimpSdk = Mailchimp.initialize(configuration)
```

This initialize the SDK and returns the new shared instance. It is recommended you initialize on app start.
Subsequently the Shared Instance can be accessed by calling ```Mailchimp.sharedInstance()```.

## Usage

### Adding A Contact

To add a contact to your Mailchimp audience, first create the Contact object using the Contact Builder. Once created pass the user in to the
createOrUpdateUser method. This will add the contact to your Mailchimp audience. If the contact already exists, their information will be updated
with the values that were passed in.

```kotlin
    val newContact = Contact.Builder("<insert email here>")
           .setMergeField("FNAME", "Example")
           .setMergeField("LNAME", "User")
           .setContactStatus(ContactStatus.SUBSCRIBED)
           .addTag("Power User")
           .build()
    val sdk = Mailchimp.sharedInstance()
    sdk.createOrUpdateContact(newContact)
```

### Updating a Contact

You may update a contact by using the same `createOrUpdateContact()` method described in the Adding a Contact section.

In addition to updating a whole contact, we provided a number of methods to update a single field on a contact. These will be executed as a separate job.
So if you wish to update multiple fields at once, we suggest using the `createOrUpdateContact()` method.

Single field update methods include

* `addTag()`: Adds a tag to the given user.
* `addTags()`: Adds multiple tags to the given user.
* `removeTag()`: Removes a tag from the given user.
* `removeTags()`: Removes multiple tags from the given user.
* `setMergeField()`: Sets or updates the merge field for a given user.
* `setMarketingPermission()`: Sets the status of a marketing permission for a given user.


### Job Status and Work Manager

The Mailchimp SDK uses [Google's Work Manager](https://developer.android.com/reference/androidx/work/WorkManager) to manager retry logic and job execution order.
This means that all operations guaranteed to execute regardless of network status and App Restart. In addition the order of execution is maintained so that multiple calls
will execute in the correct order.

For most use cases a fire and forget approach will be sufficient. However if needed, you can poll the status of jobs using the `getStatusById()` and `getStatusByIdLiveData()` methods.

`getStatusById()` will return the current status of a job.

```kotlin
val uuid = sdk.addTag("example@user.com", "ExampleTag")
val status = sdk.getStatusById(uuid)
//...
if (status == WorkStatus.FINISHED) {
    Log.i("Mailchimp SDK", "Tag Was Added")
}
```

`getStatusByIdLiveData()` will return a [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) that may be used to monitor the state of a job.

```kotlin
val uuid = sdk.addTag("example@user.com", "ExampleTag")
val statusLiveData = sdk.getStatusByIdLiveData(uuid)
statusListData.observe(
            this
            Observer {
                Toast.makeText(this, "Current Job Status: $it", Toast.LENGTH_SHORT).show()
            })
```

## Contact Schema

### Email

The Email Address is used as the unique identifier for each contact in an audience. An email address will be required for every interaction with the SDK.

### Tags

Using the SDK, users can have tags applied or removed to their contact information. Each tag is identified using a String. If tag has not been previously used on your account, it will be created.

#### Autotagging

If autotagging is enabled in the `MailchimpSdkConfiguration`, all created or updated contacts will be automatically tagged with `Android` and either `Phone` or `Tablet`.

### Merge Fields

Merge fields are a set of key value pairs that can be set on each contact. They can be customized for each audience. Common examples of merge fields would be first name, last name, and phone number.
The value of a merge field can be set and updated from the SDK. Merge fields are keyed off of a capitalized string. The Key does not include vertical bars on either end (ex. FNAME and not |FNAME|).

* Merge fields can be located on audience settings page on the mailchimp website and are also listed out after sdk-key creation.
* Merge Fields can be marked as required on the audience settings. If you attempt to create a contact without setting the required merge fields the creation will silently fail.

#### String Merge Fields
The majority of merge field types are represented as a string. This includes Text, Number, Radio Buttons, Drop Downs, Dates, Birthday, Phone Numbers, and Websites.

#### Address Merge Fields

Merge Fields of type address are not represented as a string and instead receive a custom object that follows the builder pattern. Addresses have three required fields, Address Line One, City, and Zip.
In addition there are three optional fields, Address Line Two, State, and Country. Below is an example of an Address object.

```kotlin
    val address = Address.Builder("404 Main St.", Atlanta, "30308")
            .setAddressLineTwo("apt. 101")
            .setState("Georgia")
            .setCountry(Country.USA)
            .build()
```

### Contact Status

The Contact Status represents what type of communication the user has consented to. This can either be Subscribed (will receive general marketing campaigns) or Transactional (will only receive transactional emails).
This value can only be set when the contact is created. If this is set at any other time, the new value will be ignored. By default all users will be marked as transactional if this value is not set at creation.

### Marketing Permissions

GDPR compliant audiences require users to consent individually to Email, Direct Mail, and Customized Online Advertising.
The Mailchimp SDK supports updating these permissions by supplying the Audience specific keys to the contact at time of creation or updating.
You may mark permissions as granted or denied. Simply setting these permissions will not make your audience GDPR compliant.
See the [Mailchimp GDPR Tutorial](https://mailchimp.com/help/collect-consent-with-gdpr-forms/) for a more complete description of Marketing Permissions and how to use them.

```kotlin
    val emailPermissionKey = "1234567890" // Example Key
    val mailPermissionKey = "2345678901" // Example Key
    val advertisingPermissionKey = "3456789012"
    val newContact = Contact.Builder("example@email.com")
           .setMarketingPermission(emailPermissionKey, true)
           .setMarketingPermission(mailPermissionKey, true)
           .setMarketingPermission(advertisingPermissionKey, true)
           .build()
    val sdk = Mailchimp.sharedInstance()
    sdk.createOrUpdateContact(newContact)
```

#### Retrieving Marketing Permission Keys
TODO we need to add this in for the beta release

## Demo App

### Autofilling Demo SDK Key (Optional)

On a Unix-like OS, in your home (~) folder, go into the .gradle folder and open or create a gradle.properties file.
Add the following key-value pair to have your SDK Key for the demo app filled in automatically.

```
MAILCHIMP_SDK_DEMO_KEY=<YOUR_SDK_KEY>
```

Replacing `<YOUR_SDK_KEY>` with the SDK Key you wish to use.

## FAQ

Do you have an iOS version?
>Yes! You can find it here TODO add link when we have public repo

