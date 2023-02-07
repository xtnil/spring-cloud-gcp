#!/bin/bash
WORKING_DIR=`pwd`

function fail() {
  echo "sanity check failed:"
  echo "  $1"
  exit 1
}

# checks that library list was generated and not empty
if [[ $(cat library_list.txt | wc -l) -lt 2 ]]; then fail "library list is empty"; fi

# checks that the contents of each entry in the library list is a string with
# length >= 1
libraries=$(cat $WORKING_DIR/library_list.txt | tail -n+2)
while IFS=, read -r library_name googleapis_location coordinates_version googleapis_commitish monorepo_folder; do

  non_empty_check_items=(
    "$library_name"
    "$googleapis_location"
    "$coordinates_version"
    "$googleapis_commitish"
    "$monorepo_folder"
  )
  for column in "${non_empty_check_items[@]}"; do
    if [[ -z $column ]]; then
      echo "$library_name, $googleapis_location, $coordinates_version, $googleapis_commitish, $monorepo_folder"
      fail "one of the library list cells is empty"
    fi
  done
done <<< $libraries

# checks the existence of a pom, a *AutoConfiguration.java, *Properties.java in
# each of the generated libraries
starters=$(find ../spring-cloud-previews -maxdepth 1 -name "google-*" -type d -printf "%p\n")
while IFS=' ' read -r starter_folder_raw; do

  starter_folder=$(realpath "$starter_folder_raw")
  starter_name=$(basename $starter_folder)
  base_error="generated file check for starter $starter_name"

  # check existence of pom
  if [[ ! -f $starter_folder/pom.xml ]]; then
    fail "$base_error: pom.xml not found"
  fi

  # checks for at least 1 autoconfig file
  if [[ $(find $starter_folder -name "*AutoConfiguration.java" | wc -l) -lt 1 ]]; then
    fail "$base_error: no *AutoConfiguration.java files found"
  fi

  # checks for at least 1 properties file
  if [[ $(find $starter_folder -name "*Properties.java" | wc -l) -lt 1 ]]; then
    fail "$base_error: no *Properties.java files found"
  fi

  # checks for configuration metadata resource
  if [[ $(find $starter_folder -name "additional-spring-configuration-metadata.json" | wc -l) -lt 1 ]]; then
    fail "$base_error: no additional-spring-configuration-metadata.json found"
  fi

  # checks for AutoConfiguration imports file
  if [[ $(find $starter_folder -name "org.springframework.boot.autoconfigure.AutoConfiguration.imports" | wc -l) -lt 1 ]]; then
    fail "$base_error: no org.springframework.boot.autoconfigure.AutoConfiguration.imports file found"
  fi

done <<< $starters


echo "sanity check OK"
