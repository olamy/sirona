<!---
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
## Plugins

There are several kind of plugins:

* `org.apache.commons.monitoring.gauges.Gauge` and `org.apache.commons.monitoring.gauges.GaugeFactory`: you can add your own gauges
* `org.apache.commons.monitoring.reporting.web.plugin.Plugin`: add feature to the web GUI

## Write your own gauge

To add your own Gauge you have two main solutions:

* simply implement a `org.apache.commons.monitoring.gauges.Gauge` and register it using ServiceLoader mecanism (META-INF/services/org.apache.commons.monitoring.gauges.Gauge)
* implement a `org.apache.commons.monitoring.gauges.GaugeFactory` which is registered it using ServiceLoader mecanism (META-INF/services/org.apache.commons.monitoring.gauges.GaugeFactory) and return the gauges you want to register

What is GaugeFactory designed for? Imagine a custom gauge is parameterized. You'll surely want to register it
several times with different parameters. If you use Gauge SPI you'll need to do N implementations (which makes the parameters useless).
With GaugeFactory you just need to return the built instances:

    public class MyGaugeFactory implements GaugeFactory {
        @Override
        public Gauge[] gauges() {
            return new Gauge[] { new MyGauge(1); new MyGauge(2); };
        }
    }

## Extend the reporting GUI

To extend the reporting GUI just write your own `org.apache.commons.monitoring.reporting.web.plugin.Plugin`. Here too it
relies on java ServiceLoader (SPI) mecanism.

Here is the Plugin interface:

    public interface Plugin {
        String name();
        Class<?> endpoints();
        String mapping();
    }


A plugin has basically a name (what will identify it in the webapp and in the GUI - it will be the name of the plugin tab),
a mapping, ie which base subcontext it will use for its own pages (for instance /jmx, /myplugin ...) and a class representing endpoints.

To make it more concrete we'll use a sample (the standard Hello World).

### Define the plugin

So first we define our HelloPlugin:

    public class HelloPlugin implements Plugin {
        public String name() {
            return "Hello";
        }

        public Class<?> endpoints() {
            return HelloEndpoints.class;
        }

        public String mapping() {
            return "/hello";
        }
    }

### Define the endpoints

The `HelloEndpoints` class defines all the urls accessible for the hello plugin. It uses the `org.apache.commons.monitoring.reporting.web.handler.api.Regex`
annotation:


    public class HelloEndpoints {
        @Regex // will match "/hello"
        public Template home() {
            return new Template("hello/home.vm", new MapBuilder<String, Object>().set("name", "world).build());
        }

        @Regex("/world/([0-9]*)/([0-9]*)") // will match "/hello/world/1/2"
        public String jsonWorld(final long start, final long end) {
            return "{ \"name\": \world\", \"start\":\"" + long1 + "\",\"end\":\"" + long2 + "\"}";
        }
    }

The first home method uses a template. The GUI relies on velocity and html templates needs to be in the classloader in templates directory.

So basically the home method will search for templates/hello/home.vm velocity template. It is only the "main" part of the GUI
(the tabs are automatically added). Twitter bootstrap (2.3.2) and JQuery are available.

Here is a sample:

    <h1>Hello</h1>
    <div>
        Welcome to $name
    </div>

If you need resources put them in the classloader too in "resources" folder.

Note: if you want to do links in the template you can use $mapping variable as base context of your link. For instance: &gt;a href="$mapping/foo"&lt;Foo&gt;/a&lt;.

If you want to filter some resources you can add a custom endpoint:

    @Regex("/resources/myresource.css")
    public void filterCss(final TemplateHelper helper) {
        helper.renderPlain("/resources/myresource.css");
    }

#### `@Regex`

`@Regex` allows you to get injected path segments, here is what is handled:

* HttpServletRequest
* HttpServletResponse
* TemplateHelper (should be used when you want to render a velocity template which is not in /templates and is not decorated by the default GUI layout)
* String: will inject the matching element of the regex (it is indexed = if you inject 2 strings the first one will be the first group and the second one the second group)
* Long, Integer: same as for String but converted
* String[]: all not yet matched segments of the regex

For instance `@Regex("/operation/([^/]*)/([^/]*)/(.*)")` will match `foo(String, String, String[])`.
If the url is `/operation/a/b/c/d/e` you'll get `foo("a", "b", { "c", "d", "e" })`.