cmake_minimum_required(VERSION 3.16)
project(leadforge_native LANGUAGES CXX)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
option(LEADFORGE_PYTHON "Build Python extension" OFF)

add_library(leadforge_core STATIC
    src/fingerprint.cpp
    src/scoring.cpp
    src/dedup.cpp
)
target_include_directories(leadforge_core PUBLIC include)

if(LEADFORGE_PYTHON)
  find_package(Python3 COMPONENTS Interpreter Development REQUIRED)
  find_package(pybind11 CONFIG REQUIRED)
  pybind11_add_module(leadforge_native bindings.cpp)
  target_link_libraries(leadforge_native PRIVATE leadforge_core)
endif()
