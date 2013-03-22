#!bash

PREFIX=http://localhost:8080/gps
AUTHENTICATION=--user user:pass

echo "Creating OncoCarta panel..."
curl -i $AUTHENTICATION --request POST -d name="OncoCarta" -d versionString="1.0.0" -d technology="Sequenom" $PREFIX/panel/create
curl -i $AUTHENTICATION --upload-file "data/panels/oncocarta_v1.0/panel_assays.csv" $PREFIX/panel/1

echo "Creating OncoCarta PacBio panel..."
curl -i $AUTHENTICATION --request POST -d name="OncoCarta PacBio" -d versionString="1.0.0" -d technology="PacBio" $PREFIX/panel/create
curl -i $AUTHENTICATION --upload-file "data/panels/oncocarta_pacbio_v1.0/panel_assays.csv" $PREFIX/panel/2

echo "Creating OncoCarta Sanger panel..."
curl -i $AUTHENTICATION --request POST -d name="OncoCarta Sanger" -d versionString="1.0.0" -d technology="ABI" $PREFIX/panel/create
curl -i $AUTHENTICATION --upload-file "data/panels/oncocarta_sanger_v1.0/panel_assays.csv" $PREFIX/panel/3

echo "Creating OncoCarta PacBio panel..."
curl -i $AUTHENTICATION --request POST -d name="OncoCarta PacBio" -d versionString="1.2.0" -d technology="PacBio" $PREFIX/panel/create
curl -i $AUTHENTICATION --upload-file "data/panels/oncocarta_pacbio_v1.2/panel_assays.csv" $PREFIX/panel/4

echo "Loading mutations..."
curl -i $AUTHENTICATION --upload-file "data/mutations/known_mutations.csv" $PREFIX/mutation
