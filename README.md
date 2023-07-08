```yaml
reload:
  # {name} {path} {child}
  - MyPlugin /home/user/myPluginFolder MyPlugin.jar

ftp:
  # If (client: true) transfers the files specified in the configuration to the FTP server.
  # All in all, it's pretty useless.
  # Maybe it will be useful to someone.
  client: false
  server: 192.168.0.104
  user: user
  password: password
  port: 21

destination: /home/user/myServer/plugins
```

![photo_2023-07-08_15-30-59](https://github.com/isaweye/localhost-helper/assets/130868496/29363bf3-b8cd-4ff0-ad6e-3a4448d91968)
