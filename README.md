# assetMantleClient

This project implements explorer and other web2 applications for persistence ecosystem.

## Setup

### Prerequisites:

1. Install `sdkman`:
   1. `curl -s "https://get.sdkman.io" | bash`
   2. `source "$HOME/.sdkman/bin/sdkman-init.sh"`
2. Install `Java 11.0.x`:
   1. `sdk install java 11.0.11.hs-adpt`
3. Install `sbt 1.5.5`:
   1. `sdk install sbt 1.5.5`
4. Install `PostgreSQL 11`:
   1. [MacOS](https://postgresapp.com) (Make default username and password `postgres` and `postgres` respectively)
   2. Ubuntu:
      1. `wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -`
      2. `RELEASE=$(lsb_release -cs)`
      3. `echo "deb http://apt.postgresql.org/pub/repos/apt/ ${RELEASE}"-pgdg main | sudo tee  /etc/apt/sources.list.d/pgdg.list`
      4. `sudo apt update`
      5. `sudo apt -y install postgresql-11`
      6. `sudo su - postgres` or `psql -U postgres`
      7. `psql -c "alter user postgres with password 'postgres'"`
      8. `exit`

### Starting Client in Intellij:
1. Click on `Database` on right then on `+` -> `DataSource` -> `PostgreSQL`
2. Fill the following:
   1. `User`: `postgres`
   2. `Password`: `postgres`
   3. `URL`: `jdbc:postgresql://localhost:5432/postgres`
   4. `Database`: `postgres`
   5. `Save`: `Forever`
   6. Do `Test Connection` this should succeed. Then `Apply` -> `OK`
3. Select `0.sql` contents and execute in `console`
4. Update `explorer.run.xml` with correct values and the run.
5. Click on `Database` on right then on `stack + wrench`, go to `Schema` and select `persistence` and all its tables.

### Starting Client on Terminal:
1. `sudo su - postgres`
2. `psql`
3. Copy contents of `0.sql` and paste there
4. `\q`
5. `exit`
6. Set all the environment variables in `application.conf` (They are of form `${NAME}`)
7. Create binary for client:
   1. Go to project directory in terminal.
   2. `sbt clean`
   3. `sbt dist`
   4. You get a `assetMantle-1.0.zip` file which contains binary file in `bin` folder.
8. Modify `applicaltion.conf` as per the requirements.
9. Run `./persistenceclient`

#### SFTP Scheduler:

* SFTP server:
   1. Create a new user
      1. `adduser your_username`
      2. OR `useradd -m  your_user` // will not ask for password
         `sudo passwd your_user`
      3. Go to the user
         `su - your_user`
      4. Create a .ssh directory
         `mkdir .ssh`
      5. Go inside the directory
         `cd .ssh`
      6. Create a file authorized_keys
         `touch authorized_keys`
      7. Make a ssh key pair
        `ssh-keygen`
      8. Share the public key to whoever wants to connect
         Register the public keys of other users to authorized_keys ( can use command ssh-copy-id from remote or simple copy paste also works)
      9. Exit the directory
         `cd ..`
      10. Change permissions for the directory.
          `chmod 700 .ssh`
      11. Usually we create a directory inside of home where we allow sftp uploads or other requests
          So change home directory permissions
          `chmod 755 .`
      12. `mkdir uploads/`
      13. Done!Now you can access the server via remote with
          sftp your_user@ip
      14. This will create a shell. Then you can navigate to uploads/ directory and use put and get commads

* SFTP SERVER with restricted access and no shell:
  1. Login to root
  2. `sudo adduser [username]`
  3. Create the file and insert the public key of users in this file
     `touch /etc/ssh/authorized_keys_username`
  4. Make the directory structure you want and keep the folder users can access at the last
  5. `sudo mkdir -p /var/sftp/../../second_last_Dir/accessibleDir/`
  6. Give root permissions to every folder before the last one :
     1. `sudo chown -R root:root /var/sftp/../../second_last_Dir/`
     2. `sudo chmod -R 755 /var/sftp../../second_last_Dir/`
  7. Give sftp permissions to last folder :
     `sudo chown username:username /var/sftp/../../second_last_Dir/accessibleDir/`
  8. Change sshd config file to disable shell access and disable password login:
     `sudo nano /etc/ssh/sshd_config`
     1. Match User `username`
     2. AuthorizedKeysFile  /etc/ssh/authorized_keys_%u
     3. ForceCommand internal-sftp
     4. PasswordAuthentication no
     5. ChrootDirectory /var/sftp/../../second_last_Dir
     6. PermitTunnel no
     7. AllowAgentForwarding no
     8. AllowTcpForwarding no
     9. X11Forwarding no
  9. Restart sshd to apply changes: `sudo systemctl restart sshd`
  10. Login from user as..
      `sftp -i “private_key_of_authorized_public_key” username@ip`

## Container

### Local testnet explorer

> Building container image with docker in macos will be slower as there is no native docker support

* Install docker on your machine

  * Macos: [Orbstack](https://orbstack.dev/)

  * Linux

  ```shell
  curl -sL get.docker.com | sudo bash
  docker version
  ```

* Setup keystore named `mantlekeystore` in root of the repository

* Run postgres, testnet and explorer containers

```shell
# enable buildx
export COMPOSE_DOCKER_CLI_BUILD=1
export DOCKER_BUILDKIT=1

# spin up postgres
docker-compose up -d postgres
# spin up testnet
docker-compose up -d testnet
# spin up explorer
docker-compose up -d explorer
```

#### Stop the stack, remove the data

```shell
# Stop postgres, testnet and explorer containers
docker-compose down

# Remove testnet data
docker volume client_testnet-data
# Remove postgres data
docker volume client_explorer-postgresql-data
```

### Generate dist

> Only docker with buildx required

```shell
docker buildx build \
  --output=type=local,dest=./ \
  --secret=id=git,src=$HOME/.ssh/id_rsa \ # For clienttools
  --build-arg=APP_VERSION=$(git rev-parse --short HEAD) \.
```
