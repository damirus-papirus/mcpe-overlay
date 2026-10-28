import { world, system } from "@minecraft/server";
import { ActionFormData } from "@minecraft/server-ui";

const playerSettings = new Map();

function getSettings(player) {
    let state = playerSettings.get(player.id);
    if (!state) {
        state = { coords: true, nightVision: false, speed: false, creative: false };
        playerSettings.set(player.id, state);
    }
    return state;
}

function message(player, text) {
    try { player.sendMessage("§a[MCPE Overlay] §f" + text); } catch (_) {}
}

function toggleEffect(player, state, key, effectId, label, amplifier = 0) {
    try {
        if (state[key]) {
            player.removeEffect(effectId);
            state[key] = false;
            message(player, label + " выключено.");
        } else {
            player.addEffect(effectId, 20 * 60 * 60, {
                amplifier,
                showParticles: false
            });
            state[key] = true;
            message(player, label + " включено на 60 минут.");
        }
    } catch (_) {
        message(player, "Не удалось изменить эффект. Проверь, что аддон активен в этом мире.");
    }
}

async function openMenu(player) {
    const state = getSettings(player);
    const form = new ActionFormData()
        .title("MCPE Overlay")
        .body(
            "§7Игровые функции аддона\n\n" +
            "§fКоординаты HUD: " + (state.coords ? "§aВКЛ" : "§cВЫКЛ") + "\n" +
            "§fНочное зрение: " + (state.nightVision ? "§aВКЛ" : "§cВЫКЛ") + "\n" +
            "§fСкорость: " + (state.speed ? "§aВКЛ" : "§cВЫКЛ") + "\n" +
            "§fТворческий режим: " + (state.creative ? "§aВКЛ" : "§cВЫКЛ") + "\n\n" +
            "§7Эти функции работают внутри мира с активным аддоном. " +
            "Плавающая кнопка Android сама по себе не управляет движком Minecraft."
        )
        .button("Координаты HUD: " + (state.coords ? "ВЫКЛ" : "ВКЛ"))
        .button("Ночное зрение: " + (state.nightVision ? "ВЫКЛ" : "ВКЛ"))
        .button("Скорость: " + (state.speed ? "ВЫКЛ" : "ВКЛ"))
        .button("Творческий режим / полёт: " + (state.creative ? "ВЫКЛ" : "ВКЛ"))
        .button("Закрыть");

    try {
        const result = await form.show(player);
        if (result.canceled || result.selection === undefined) return;

        switch (result.selection) {
            case 0:
                state.coords = !state.coords;
                message(player, "HUD координат " + (state.coords ? "включён." : "выключен."));
                break;
            case 1:
                toggleEffect(player, state, "nightVision", "night_vision", "Ночное зрение");
                break;
            case 2:
                toggleEffect(player, state, "speed", "speed", "Ускорение", 1);
                break;
            case 3:
                try {
                    if (!state.creative) {
                        player.runCommand("gamemode creative @s");
                        state.creative = true;
                        message(player, "Творческий режим включён. Полёт доступен.");
                    } else {
                        player.runCommand("gamemode survival @s");
                        state.creative = false;
                        message(player, "Переключён режим выживания.");
                    }
                } catch (_) {
                    message(player, "Не удалось сменить режим. Проверь разрешения и читы в настройках мира.");
                }
                break;
            default:
                return;
        }

        system.runTimeout(() => openMenu(player), 2);
    } catch (_) {
        message(player, "Не удалось открыть меню. Закрой другие игровые формы и попробуй снова.");
    }
}

world.afterEvents.playerSpawn.subscribe((event) => {
    const player = event.player;
    getSettings(player);

    if (event.initialSpawn) {
        system.runTimeout(() => {
            try {
                player.runCommand("give @s compass 1");
                message(player, "Компас меню выдан. Используй его, чтобы открыть MCPE Overlay.");
            } catch (_) {
                message(player, "Выдай себе компас командой /give @s compass.");
            }
        }, 30);
    }
});

world.afterEvents.itemUse.subscribe((event) => {
    try {
        if (event.itemStack && event.itemStack.typeId === "minecraft:compass") {
            openMenu(event.source);
        }
    } catch (_) {}
});

system.runInterval(() => {
    for (const player of world.getAllPlayers()) {
        const state = getSettings(player);
        if (!state.coords) continue;

        try {
            const p = player.location;
            player.onScreenDisplay.setActionBar(
                "§bXYZ §f" +
                Math.floor(p.x) + "  " +
                Math.floor(p.y) + "  " +
                Math.floor(p.z)
            );
        } catch (_) {}
    }
}, 10);

world.afterEvents.playerLeave.subscribe((event) => {
    playerSettings.delete(event.playerId);
});
