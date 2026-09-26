In order to run the app please run the following command once downloading
```bash
sbt "run <destination> <parallelism>"

destination - destination folder of the folder with files (Absolute path)
parallelism - how many files will be processed in parallel. Usually should be the amount of CPU cores or virtual cores. If not provided, defaulted to 4