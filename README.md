# Projet 4SL01

## Tester le projet

Pour tester le projet, il suffit de vous rendre dans le dossier `outputs`. Vous y trouverez 3 fichiers .jar qui permettent de lancer le projet.
- Le fichier `RobotSim.jar` permet de lancer l'affichage graphique
- Le fichier `WebServer.jar` permet de gérer la sauvegarde de données via les sockets.
- Le fichier `Microservice.jar` permet de lancer la simulation en utilisant Kafka.

Pour lancer le test, commencez par démarrer Kafka.

Une fois Kafka actif, vous pouvez lancer le script `run.sh` qui se charge d'exécuter tous les fichiers jar en parallèle.

Les usines sont sauvegardées et chargées dans le répertoire où le code s'exécute.

Lorsque vous sauvegardez une usine, il vous suffit donc de simplement rentrer son nom, par exemple `mon_usine` et le fichier `mon_usine.factory` sera automatiquement créé.

## Voir le code

Normalement, vous pouvez importer les projets `microservice` et `ProjetRobotSim` dans eclipse. 

Tenez compte que ce projet a été développé et compilé via IntelliJ, je n'ai donc pas testé la compilation sous eclipse.

De plus, le WebServer et le Microservice sont censés être exécutés au même endroit pour correctement fonctionner (les fichiers de sauvegarde de l'usine sont sauvegardés à l'endroit où Java s'exécute). C'est pourquoi tester directement le microservice via eclipse ne fonctionnera pas correctement. Si vous souhaitez tout de même le lancer via eclipse, le plus simple est de copier les fichiers `.factory` présents dans le projet `microservice` dans le projet `ProjetRobotSim`. Si vous sauvegardez de nouvelles usines, il faudra faire l'opération inverse : sauvegarder les fichiers créés dans `ProjetRobotSim` dans `microservice`. C'est pourquoi, encore une fois, il est plus simple de gérer cela via les .jar déjà créés.

Pourquoi ce choix de tout sauvegarder au même endroit ? Il se trouve que sur IntelliJ, je peux lancer depuis le projet `microservice` les différents projets qu'il importe, en particulier `WebServer` et `RobotFactory`. De plus, cela permettait d'éviter d'avoir à rentrer le chemin d'accès complet pour sauvegarder des usines.
