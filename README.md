# Remove ButterKnife

Remove ButterKnife binding annotations, generate `bindView` method and `findViewById` codes instead.  

## Install

- install from release jar

  - Navigation -> File -> Settings -> Plugins -> On the top side, click 'gear' icon -> install plugin form disk -> choose the jar you download

- install from IntelliJ Plugin Repository

   - Navigation -> File -> Settings -> Plugins -> search 'Generate FindViewById'

## Usage

Right mouse button > Refactor > Remove ButterKnife

## Build 

Kotlin dependency `<depends>org.jetbrains.kotlin</depends>` must import kotlin plugin jars as project dependency first, 
import dependency from `Project Structure > Module > Dependencies`, choose kotlin plugin your IDEA installation path > plugin > kotlin > lib. 