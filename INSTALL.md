# Installation

At this time, there are no official builds provided. You can build CI Components by cloning its [git repository](https://github.com/cicomponents/cicomponents) and building it manually:

```
git clone https://github.com/cicomponents/cicomponents
cd cicomponents
gradle dist
```

The container will be available in `dist/target/assembly`, and you can start it using `bin/start` and you can connect to it using `bin/client` (login: ci, password: ci). Alternatively, you can run it in foregroudn with `bin/cicomponents`.

## Setup

### Publicly available URL

For many components, a publicly available URL is necessary for callbacks. You can configure it in the console:

```
config:edit org.cicomponents.core.Configuration
config:property-set url http://ci.yourproject.org
config:update
```

or by editing `etc/org.cicomponents.core.Configuration.cfg`:

```
url = http://ci.yourproject.org
```

If you want to test CI Components on your local computer, a tunnel like [ngrok](http://ngrok.com) is recommended.
