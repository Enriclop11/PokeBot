var combatInterval;

function getPokemonCombatData() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        try {
            if (this.readyState === 4 && this.status === 200) {
                const combatData = JSON.parse(this.responseText);

                console.log("combatData", combatData)

                if (combatData !== null) {
                    showScreen();
                    this.combatData = combatData;
                    updatePokemonInfo(1, combatData[0]);
                    updatePokemonInfo(2, combatData[1]);
                } else {
                    clearScreen();
                }
            }

        } catch (e) {
            clearScreen();
        }
    };
    xhttp.open("GET", "/pokemon/combat", true);
    xhttp.send();
}

function showScreen() {
    document.getElementById('pokemonArena').style.display = 'flex';
}

function clearScreen() {
    document.getElementById('pokemonArena').style.display = 'none';
}

function updatePokemonInfo(pokemonIndex, pokemon) {
    const pokemonInfo = document.getElementById(`pokemonInfo${pokemonIndex}`);
    const pokemonImage = pokemonInfo.querySelector('.pokemon');
    const pokemonHP = pokemonInfo.querySelector('#pokemonHP' + pokemonIndex);

    const hpPercentage = (pokemon.currentHp / pokemon.hp) * 100;

    pokemonHP.textContent = `HP: ${pokemon.currentHp}/${pokemon.hp}`;

    const hpBar = pokemonInfo.querySelector('.hp-bar');
    hpBar.style.width = hpPercentage + '%';

    hpBar.style.backgroundColor = getColorForHP(hpPercentage);

    pokemonImage.src = pokemon.frontSprite;
}


function getColorForHP(percentage) {
    if (percentage > 70) {
        return 'green';
    } else if (percentage > 30) {
        return 'yellow';
    } else {
        return 'red';
    }
}


getPokemonCombatData();
setInterval(getPokemonCombatData, 5000);