Docker



is a virtualization software

is a containerization platform



\- Packages application with all the dependencies, configurations,

system tools and runtime



rather than sharing the code + set, i.e Jar + readme.md

up instructions(what to be installed, version of it, how it to be configured)



share the complete environment under which it

worked on u r system , so that

they could just run that single large artifact directly

and get with the same environment under which the code worked

in u r system



\-----------------------------------------



DEVELOPMENT PROCESS BEFORE CONTAINERS



In my system say i developed spring boot

application with dependencies of postgres, redis and

kafka for messaging, and it works on Java 21, JDK 21

MAVEN 3.0 version, redis 7.0, kafka 1.0 and postgres 7.0

on win 11



i can share the code via GitHub not an issue

i can share a read me of what are the software

required to make the application run

say u need redis, kafka blaw blaw



if my client side developer wants to run and test his

frontend api calls being done rightly or not?



they gotta do

&#x20;

1.clone the code from GitHub

2.Install java of specified version

3\. ......

4\. ...

5.install all of the dependencies



on top of that gotta ensure it doesn't confict

with their os or working environment





high possibilities for things to go wrong



rather how good it would be

if they just has to do some commands

magical command



"hey someone get me this" aka

docker run postgres

docker run Redis





and also allows to run multiple versions of the same

software, that allows u to have

for the different application needs



that is what docker offers for u as a developer





Standardize process of running any service on any local dev environment



\----------------------------------------------------------



DEPLOYMENT PROCESS BEFORE CONTAINERS



similar to the problems faced  by the frontend team

ops team can also face



instead of giving code artifact Jar i give with

bigger picture of it, a artifact that carries the

entire environment needs



ops only may have to configure runtime



\----------------------------------------------------------



How docker differs from the VM(virtual machines)



a. before that how OS works?

b. before that what is the difference b/w the OS

and Kernel?



OS is a suite of programs, where kernel is one of the

program

kernel is the one that communicates with the hardware

almost anything the system does is done by the Kernel



os is the again a software, that instructs the kernel

to do things and on top of kernel it has interfaces

and drivers management like n/w i/o

application -> system calls -> kernel -> h/w





in general we have

OS APPLICATION LAYER   (user space)

&#x09;|

&#x09;|

OS KERNEL LAYER 	(kernel space)

&#x09;|

&#x09;|

PHYSICAL HARDWARE



docker and vm both are virtualization tool, they virtualize what



\-- VM virtualizes the, application layer + kernel layer

when vm image on host it runs with the own os not the os of the

host system



\-- Docker virtualizes the Application layer + some services and apps installed on that layer, host come up with their own kernel layer



\----------------



**VM VIRTUALIZES THE HARDWARE FOR MULTIPLE OS, MAKING THE BELIEVE A BELOVELANT LIE OF THAT**

**THE OS HAS THE FUL CONTROL OF THE HARDWARE, WHILE IT HAS ONLY ACCESS TO A PORTION OF HARDWARE**

**THAT TOO VIA THE HYPERVISOR**



**ON THE OTHER HAND**



**DOCKER VIRTUALIZES THE SOFTWARE FOR MULTIPLE PROCESS, MAKING THEM BELIEVE THAT THEY ARE**

**THE SOLE PROCESS RUNNING ON AN INDEPENDENT MACHINE, IT IS MADE POSSIBLE VIA THE**

\*\*KERNEL FEATURES LIKE CGROUPS, NAMESAPCES...

MAKING THEM HAVE OWN NETWORK SPACE, NAME SPACE ADN ALLL\*\*



\------------------





\----------------------------------------------

docker images, couple of MBS              	Vm images, copule of GBS



container starts quickly 		  	VM takes minutes to start

&#x09;

compataible with only

Linux distro					compatible with all os

&#x09;					as it carries it own os



\----------------------------------------------------

issue with docker?



Docker depends on the Linux specific features like namespace, cgroups and other

hence docker can work only on an Linux environment



\-----------------------------------------------------



Docker in other os environments



Docker Desktop is an windows application that

comes with a docker-distro a distribution like ubuntu/Debian

that allows the user applications to communicate with the Linux kernel



components



WSL: Windows Subsystem For Linux, contains the actual Linux Kernel



docker-desktop: a distro installed with the DockerDesktop windows application,

docker-desktop distro runs over the Linux kernel in the WSL like Debian and Ububut



it is the one that runs the docker engine aka dockerd docker daemon



DockerDesktop(GUI)/DockerCli: or winows.exe they pipes the docker commands like docker run via the

sockets/api to the docker-desktop:distro in turn that executes commands on the WSL:KERNEL:LINUX:DOCKERD





\----------------------------------------------------------------------



Practice



1.Install Docker Desktop

comes with

&#x09;1.docker engine (a server with long running daemon process

&#x09;dockerd), manages images and containers

&#x09;2.Docker cli-client - cli to interact with the docker server

&#x09;3.GUI client

&#x09;and so many...



\----------------------------------------------------------------------



Docker Image \& Docker container



Docker Image



A Docker image is a packaged, read-only application runtime blueprint.

An executable artifact



It typically contains:



1\. Application artifact

&#x20;  - app.jar



2\. Runtime dependencies

&#x20;  - JDK/JRE

&#x20;  - required libraries



3\. Linux user space filesystem layers

&#x20;  - basic filesystem structure and binaries



4\. Runtime metadata

&#x20;  - startup command

&#x20;  - env defaults

&#x20;  - exposed ports





Docker Container



A Docker container is a running isolated instance created from a Docker image.



\----------------------------------------------------------------------



Docker CLI Commands



\*\*0.\*\*list images

&#x09;docker images



\*\*1.\*\*pull an image:

&#x09;s: docker pull <image-name>

&#x09;e: docker pull nginxdemos/hello



2.run an image

&#x09;s:docker run <image-name>

&#x09;e:docker run redis:7

&#x09;

above will start the image on isolated virtual environment

and it not expose it to the host os that is

u won't be able to access the running instance on u r host machine

as it technically running on Linux OS



&#x09;s: docker run -d -p <host\_port>:<container\_port> --name <container\_name> <image\_name>

&#x09;

&#x09;	-d     : detach, make the container run on the bg without hanging in the terminal,

&#x09;		details of the container can be viewed via the docker logs <container-name

&#x09;

&#x09;	--name : name to the container/running instance of an image

&#x09;

&#x09;	-p     : port mapping, forwards any traffic received on the HOST:PORT TO CONTAINER:PORT
if this not mentioned, the process running on the container port will never be accissble

&#x09;	to the host

&#x09;

&#x09;s: docker run -p 8080 imagename

&#x09;	here only the container port is mentioned and hence docker binds the traffic

&#x09;	of the host to any random port, we can see that port by docker ps





&#x09;s: docker run -P <image-name>

&#x09;	-P: publishes all EXPOSED container ports to random available host ports
without publishing, the container is not directly accessible from the host machine,

&#x09;	though it may still be reachable from other containers/networks.

&#x09;

&#x09;s:docker run image -e SERVER\_PORT=8090

&#x09; -e: let set the environment variables



3.List containers

&#x09;s: docker container ls -a

&#x09;-a: shows all the container details, running, containers closed earlier

&#x09;

&#x09;s: docker container ls -q

&#x09;-q: returns the container id alone



4.Stop the container

&#x09;s: docker stop <container\_name/id>

&#x09;

5.Force stop aka kill

&#x09;s: docker kill <container\_name/id>



6.Log container executions

&#x09;s: docker logs <container\_id>



7.remove container

&#x09;s: docker rm <container\_name>



8.remove image

&#x09;s: docker rmi <image\_name/id>



9.force remove container

&#x09;s: docker rm -f <container\_name/id>

removes even the running containers



10.start a container(stopped one)

&#x09;s:docker start <container\_id>



11.execute command on the container

&#x09;s:docker exec <container\_name> <command>

&#x09;e:docker exec my\_redis redis-cli



12.interactive terminal

&#x09;s: docker -it run <image\_name>

&#x09;//attaches a interactive terminal to the process



&#x09;s: docker exec -it <container\_name> <command>

&#x09;e: docker exec -it my\_redis redis-cli

&#x09;//attaches the interactive terminal to the redis-clis, whcich expects a keyboard input

&#x09;//allows u to interact with the redis server via redis-cli, in interactive terminal



\-it: not meant to used with all the process





13\. environment variables

&#x09;s:docker run -e key=value image

&#x09;

&#x09;s:docker run --env-file <env-file-path> image

&#x09;

&#x09;note: --env-file accepts any filename. The filename/extension is irrelevant.

&#x09;What matters is that the file content follows Docker's environment-variable format:

&#x09;KEY=value



\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_



Docker registries



A storage and distribution system for Docker images



official images available from applications like redis, postgres, and are maintained by

software authors or in a collaboration with docker community



Docker Hub: One of the biggest Docker Registry hosted By docker themselves





\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_



Public and private docker registries



public: docker hub, anyone can access

private: provides restricted access to the docker images, basically not available to public



\-------------------------------------------------------------------

Registry vs Repository



Registry: service providing storage, collection of repositories \[DOCKER HUB, AWS ECR]



Repository: collection of related images, with same names but different versions \[REDIS,]



docker hub allows to host private or public repositories



\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_



DOCKER FILE



\- contains definition of how to build an image from our application,

docker file is the one on which these commads are written, to assemble an image





docker files usually starts from, base or parent image: it is a docker image that u r image is based on



for Java application may be: java/jdk





how to write docker file

commands used



1.FROM: Specifies the base image for the current build stage.



2.RUN: execute inside a temporary build container during image build



3.COPY: copies files from the host build context into the image filesystem during build.

COPY . .

&#x20;



4.WORKDIR: Sets the working directory for all the following commands\[nana says: like changing into directory, cd .]



5.Entry point: the main command that starts the application or makes the container executable



6.CMD: provides the default command or default arguments to ENTRYPOINT.





The ENTRYPOINT specifies a command that will always be executed when the container starts.

The CMD specifies arguments that will be fed to the ENTRYPOINT.



ENTRYPOINT \["ls"]

CMD \["-l"]





docker does: ENTRYPOINT + CMD, while starting the container
so effectively, ls  -l get executed



u can override as

docker run image -a, effectively ls -a, CMD\[l] got overridden



7\. EXPOSE <port-number>



Adds image metadata/documentation about which container ports are intended to be used/listened on by processes inside the container.



EXPOSE does NOT actually publish ports to the host machine.



Multiple EXPOSE instructions are possible.



Example:



EXPOSE 8080 8090



When the container is started using -P,

Docker automatically binds random ephemeral host ports to all EXPOSED container ports.



If applications/processes inside the container are actually listening on those ports,

Docker forwards the traffic correctly.



Example in Spring Boot:



Main API port       : 8080

Actuator endpoints  : 8090



8.ENV SEVER.PORT=8080, let set the environment variable, while building the image,
can be overridden by the  -e later starting the container itself

\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_



BUILD IMAGE





docker build -t <image\_name:tag> .

. indicates current folder, where the docker file exists





Docker image consists of layers



each instruction in the docker file creates one own layer

these layers are stacked and each one is the delta of change from the previous layers





\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_



DOCKER MULTI STAGE BUILD



\-contents that are need to build the image, but are not need in the final image to run the app



multi stage build allows to exclude the build dependencies from the final image

creates multiple temporary images during the build process

and retains only the final image to run the application





DOCKER MULTI STAGE BUILD



Multi-stage build helps exclude build dependencies and temporary build artifacts from the final runtime image.



Each FROM instruction starts a new build stage with its own fresh filesystem lineage.



Within a stage, Docker instructions create layered filesystem snapshots.



During RUN instructions, Docker uses temporary build containers/environments to execute commands and commit resulting filesystem changes as layers.



Artifacts from previous stages are NOT automatically inherited into later stages.

Only explicitly copied files using COPY --from=<stage> are transferred across stages.



Docker retains only the filesystem/layers of the final stage in the final runtime image,

while intermediate stage filesystems may still remain cached locally for future build optimization.







\#a.BUILD STAGE

FROM maven:3.9-eclipse-temurin-21 AS build



WORKDIR /app



COPY pom.xml .

RUN mvn dependency:go-offline



COPY src ./src

RUN mvn clean package -DskipTests



\#b. RUNTIME

FROM eclipse-temurin:21-jre



WORKDIR /app



COPY --from=build /app/target/\*.jar app.jar



CMD \["java", "-jar", "app.jar"]

\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_\_



Docker volumes



\- are used for data persistence in docker



like db or state-full application





in the containers like the database

the data is stored in the container file system

say somewhere like

/var/lib/postgresql/data



container writable filesystem layer gets deleted when the container is removed or the container itself is recreated



note: data not lost on restart or start-stop but only on restart or recreation



Data stored inside the container writable layer is lost

when the container is removed and recreated



docker volume sorts this out by allowing us to mount the

host filesystem directory outside container lifecycle to the docker container file system



\------------------------------------------------------------



important Linux concept to be understood

when we say mount --bind /real-storage /var/lib/PostgreSQL/data



here it means expose the same file tree /realsotrage via the

2 namespaces /real-storage and the /var/lib/PostgreSQL/data



it doesn't mean that the 2 file exists and they are kept in sync or copied or whatever

it is just that we are seeing the same filesystem subtree exposed through different paths/namespaces



types:



bind mount: user-specified storage

volume mount: docker handles the storage



\-------------------------------------------------------------



* The mounted directory becomes shared storage between

host and container.



* Docker volumes mount persistent storage into a container path,

allowing data to survive container recreation.



* Volumes are independent Docker resources and are not tied to

a specific container lifecycle.



* When a volume is mounted into a container path,

the mounted storage overrides/hides the original container directory

at that mount point.



3 docker volume types



* 1.host volume type(bind-mount)

&#x09;s: docker run -v <host-directory-path>:<container-directory-path> image

&#x09;e: docker run -v home/mount/data:var/lib/postgres/data



mounts the specified host filesystem directory

into a container filesystem path



note: these directories are not managed by the docker hence we can't find them under

docker volume ls





* 2.Anonymous volumes(volume mount)

&#x09;s: docker run -v <container-directory-path> iamge

&#x09;e: docker run -v var/lib/postgres/data



here the docker itself creates a directory on host file system and mount the same to the

container virtual file system



docker does something like: /var/lib/docker/volumes/random-hash/\_data



* 3.name volumes(volume mount)



docker run -v name:<container-directory-path>, similar to the anonymous volumes

but it will create a alias/reference for the volume automatically created by docker in the host

system, hence it can be referenced later with the name rather that to bother about the

docker created directory path



it is mostly used in the production



\-----------------------------------------------------------------------

docker volume commands





docker volume ls

\# list named and anonymous volumes



docker volume create <volume-name>

\# create named volume



docker volume inspect <volume-name>

\# inspect volume metadata and mountpoint



docker volume rm <volume-name>

\# remove volume



docker volume prune

\# remove unused dangling volumes

\-------------------------------------------------------------------------

