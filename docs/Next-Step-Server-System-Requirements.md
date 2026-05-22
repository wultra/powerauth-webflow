# Next Step Server System Requirements

The HW requirements for the default Docker images are low and current commodity hardware will be able to run the software nicely. The images should run fine on most container management platforms in multiple instances. If in doubt, use following HW configuration. For production setup we recommend running in two instances at least to achieve High Availability.

## Hardware Platforms

Following hardware platforms are supported by Next Step Server Docker images:
- amd64 (also known as x86_64)
- arm64v8

## Hardware Requirements of Basic Docker Images
### Minimal
- CPU 1×core, 2.0GHz
- 1GB free RAM for Docker
- 1GB free disk space

### Recommended
- CPU 2×core, 2.0GHz
- 4GB free RAM for Docker
- 2GB free disk space

For HA setup add more instances with similar configuration.

## Database Platforms

The Next Step Server can run without any modifications on PostgreSQL and Oracle databases.

| Database Platform | Minimal Supported Version |
|-------------------|---------------------------|
| PostgreSQL        | 14                        |
| Oracle DB         | 19c                       |

The Next Step Server can be operated on earlier versions of the DB platforms, but the versions above (and newer) are tested and supported. In general we aim to align our support with support of database vendors. Next Step Server uses PostgreSQL database as the primary datastore type. While it is possible to configure the images to use other database engine as well (Oracle, etc.), we recommend PostgreSQL as the stable, performant and open choice.

## Hardware Requirements of Database platform

For model case of installation with 500 000 users we recommend:
- CPU 2×core, 2.0GHz
- 8GB minimum RAM
- 60GB free disk space (estimated size after year of operation) 
- 5GB database size yearly increment 

The configuration can be scaled down according to the number of users. E.g. for installation with 100 000 users (in case of PostgreSQL):
- CPU 1×core, 2.0GHz
- 4GB minimum RAM
- 32GB free disk space

