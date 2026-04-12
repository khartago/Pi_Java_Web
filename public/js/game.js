function updateTimer(plant) {

    plant.timer--;

    if (plant.timer <= 0 && plant.status !== "DEAD") {

        plant.status = "DEAD";

        fetch('/plant/dead', {
            method: 'POST'
        });

        console.log("Plant died → email sent");
    }
}