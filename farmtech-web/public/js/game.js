(() => {
    const root = document.getElementById('farm-game-root');
    if (!root) return;

    const cells = Array.from(root.querySelectorAll('.game-cell'));
    const toolButtons = Array.from(root.querySelectorAll('[data-tool]'));
    const status = document.getElementById('game-status');
    let selectedTool = null;

    const plants = cells.map(() => ({
        timer: 30,
        level: 1,
        waterCount: 0,
        alive: true,
    }));

    const setStatus = (msg) => {
        if (status) status.textContent = msg;
    };

    const render = () => {
        cells.forEach((cell, i) => {
            const plant = plants[i];
            const img = cell.querySelector('[data-plant]');
            const timer = cell.querySelector('[data-timer]');
            if (!img || !timer) return;

            if (!plant.alive) {
                img.src = '/images/dead.png';
                timer.textContent = 'DEAD';
                cell.classList.add('is-dead');
                return;
            }

            cell.classList.remove('is-dead');
            const stageImg = plant.level >= 3 ? 'tommato.3.png' : plant.level === 2 ? 'tommato.2.png' : 'tommato.1.png';
            img.src = '/images/' + stageImg;
            timer.textContent = plant.timer + 's';
        });
    };

    const markDead = async () => {
        try {
            await fetch('/plant/dead', { method: 'POST', headers: { 'X-Requested-With': 'XMLHttpRequest' } });
        } catch (_e) {}
    };

    toolButtons.forEach((btn) => {
        btn.addEventListener('click', () => {
            selectedTool = btn.getAttribute('data-tool');
            toolButtons.forEach((b) => b.classList.remove('btn-primary'));
            btn.classList.add('btn-primary');
            setStatus('Outil selectionne: ' + selectedTool);
        });
    });

    cells.forEach((cell) => {
        cell.addEventListener('click', () => {
            const i = Number(cell.getAttribute('data-index'));
            const plant = plants[i];
            if (!selectedTool || !plant || !plant.alive) return;

            if (selectedTool === 'shovel') {
                plant.alive = false;
                markDead();
            } else if (selectedTool === 'manure') {
                plant.timer += 15;
            } else if (selectedTool === 'water') {
                plant.timer = 30;
                plant.waterCount += 1;
                if (plant.waterCount >= 3 && plant.level < 3) {
                    plant.level += 1;
                    plant.waterCount = 0;
                }
            }

            render();
        });
    });

    setInterval(() => {
        plants.forEach((plant) => {
            if (!plant.alive) return;
            plant.timer -= 1;
            if (plant.timer <= 0) {
                plant.alive = false;
                markDead();
            }
        });
        render();
    }, 1000);

    render();
})();
