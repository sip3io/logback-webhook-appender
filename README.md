# Logback Webhook Appender

This project aims to monitor application logs and send `WARNING` and `ERROR` logging events to configured webhook.

Appender configuration is very generic what makes it possible to use almost unlimited amount of notification channels.
More sophisticated integrations might be done using 3rd party services like Zappier.
 
## 1. Usage Example

To add `logback-webhook-appender` to your project you will need to specify an alrenative maven repository in `pom.xml` as it's shown below:

```
    <repositories>
        ...
        <repository>
            <id>sip3-releases-ce</id>
            <name>SIP3 Releases CE</name>
            <url>https://maven.sip3.io/releases-ce</url>
        </repository>
        ...
    </repositories>
```

After that just add `logback-webhook-appender` to the project's dependencies and configure `logback.xml`. You can find detailed usage example in [this](https://dev.to/sip3/how-to-monitor-remote-jvm-applications-with-logback-webhook-appender-2oc5) blog post.


## 2. Support

If you have a question about this project, just leave us a message in our community [Slack](https://join.slack.com/t/sip3-community/shared_invite/enQtOTIyMjg3NDI0MjU3LWUwYzhlOTFhODYxMTEwNjllYjZjNzc1M2NmM2EyNDM0ZjJmNTVkOTg1MGQ3YmFmNWU5NjlhOGI3MWU1MzUwMjE) and [Telegram](https://t.me/sip3io), or send us an [email](mailto:support@sip3.io). We will be happy to help you.   
