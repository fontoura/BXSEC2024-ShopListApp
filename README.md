# Shop List App for Android

> 🇧🇷 Para ver este arquivo em português, clique [aqui](./README-pt.md)

This is an open-source implementation of a shop list app for Android.

This was originally implemented to be used for [ThaySolis](https://github.com/ThaySolis)'s presentation on reverse engineering for the 2024 Conferência Baixada Santista Security (BXSEC). To set up and use the app, follow these steps:

1. Set up a server machine with a public IP.
2. Install and launch the API server on the server machine.
3. Create a DNS record pointing to the public IP of the server machine.
4. Update the API server URL within the Android source code.
5. Build the APK.
6. Install the APK on your device.

For more detailed instructions, refer to the guide below.

# Guide

## Set up a server machine with a public IP

To run the API server, you need a machine with a public IP address. Most likely, you'll use a cloud provider like AWS for this. Additionally, you must configure firewall rules to allow external access to port `8443` of that machine can be accessed from the anywhere (i.e. `0.0.0.0/0`). Follow the steps below if you're using AWS:

- **Create an EC2 instance**: Select a Linux image (e.g., Ubuntu) and ensure it has a public IP address.
- **Create and configure a Security Group**: Set up a custom Security Group and associate it with your EC2 instance.
- **Update Security Group rules**: In the Security Group, modify the ingress rules to allow TCP connections from `0.0.0.0/0` (i.e., anywhere) to port `8443`.

# Install and launch the API server on the server machine.

After setting up the server machine, ensure that Go is installed. You can follow the [official installation guide](https://go.dev/doc/install) or, if you're using Ubuntu, you can simply run the following command:

```bash
apt install go
```

Once Go is installed, transfer the `server` folder to your server machine. It’s recommended to place it in your home directory, but any location on the file system will work. The steps you need to take to do this will vary depending on your cloud provider and the configuration of the machine.

Navigate to the `server` folder in the server machine and compile the API server by running:

```bash
go build .
```

If the build succeeds, a new file named `shoplistvulnerableserver` will be created in the same folder. This is the API server executable.

To start the server, run the following command. It’s best to use a tool like `nohup` to keep the server running even if the console session is closed:

```bash
nohup ./shoplistvulnerableserver &
```

## Create a DNS record pointing to the public IP of the server machine

Next, you need to create a DNS record that points to the public IP of your server. There are several services available for this, such as [DuckDNS](https://www.duckdns.org/).

The DNS entry you create will serve as the base URL for your API server. For example, if your DNS entry is `whatever.duckdns.org`, the base URL for your API will be `https://whatever.duckdns.org:8443`.

Make sure to take note of your specific URL, as you'll need it for the following steps.

## Update the API server URL within the Android source code

Locate the `ShopListApplication.java` file in the source tree ([this file](app\src\main\java\com\github\fontoura\sample\shoplist\ShopListApplication.java)).  In the Java code, find the constant `HTTP_ENDPOINT`, which looks like this:

```java
private static final String HTTP_ENDPOINT = "https://{INSERT_URL_HERE}:8443";
```

Replace it with the base URL of your API server, for example:

```java
private static final String HTTP_ENDPOINT = "https://whatever.duckdns.org:8443";
```

Make sure to update the URL to reflect your specific DNS entry.

## Build the APK

Make sure you have Android Studio installed. If not, download it from the [official website](https://developer.android.com/studio) and install it.

1. Open the project in Android Studio.
2. Once the project is fully loaded, navigate to **Build > Generate Signed App Bundle / APK...**.
3. In the popup window, select the **APK** option.
4. If you don't already have a keystore, create one along with a key, then proceed.
5. Select the **release** build variant.

The APK will be generated in the `app/release` folder.

## Install the APK on your device

To install the newly generated APK on your device, you have two options:

1. Using ADB:
    - Connect your device to your computer via USB.
    - Run the following command in your terminal or command prompt:
    ```
    adb install <path-to-file>/release.apk
    ```
2. Manual Installation:
    - Transfer the APK file to your device (e.g., via USB or cloud storage).
    - Open the APK on your device and follow the prompts to install it.

## License
This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.
