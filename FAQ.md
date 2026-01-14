# Overview

Contains [frequently asked questions](https://en.wikipedia.org/wiki/FAQ)
related to Trifa Tox Client.


# List of Content

1. [What are the circles next to a contact?](#what-are-the-circles-next-to-a-contact)


# What are the circles next to a contact?

There are 3 circles on the contact item, see

![Example Trifa Contact List](https://raw.githubusercontent.com/zoff99/ToxAndroidRefImpl/refs/heads/zoff99/dev003/images/trifa-contact-list.png)

* the circle around avatar (`1`) tells the connection type, like
    * <span style="background-color: green; color: white">green</span> -> friend is online via UDP
    * <span style="background-color: yellow; color: black">yellow</span> -> friend is online via TCP
    * <span style="background-color: gray; color: white">grey</span> -> friend is offline
* the medium circle (`2`) inner circle tells the connection status, like
    * <span style="background-color: green; color: white">green</span> -> friend has toxproxy (for offline messages) and proxy is online
    * <span style="background-color: yellow; color: black">yellow</span> -> friend has PUSH notification feature
    * <span style="background-color: red; color: white">red</span> -> friend has toxproxy (for offline messages) but it is offline
* the medium circle (`2`) outer circle tells the connection status, like
    * <span style="background-color: green; color: white">green</span> -> friend is online
    * <span style="background-color: red; color: white">red</span> -> friend is offline
* the smallest circle (`3`) tells the contact/user status, like
    * <span style="background-color: green; color: white">green</span> -> friend set status to "I'm available"
    * <span style="background-color: yellow; color: black">yellow</span> -> friend set status to "I'm away"
    * <span style="background-color: red; color: white">red</span> -> friend set status to "I'm busy"



