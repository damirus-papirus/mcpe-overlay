import { world, system } from "@minecraft/server";
import { ActionFormData } from "@minecraft/server-ui";

const settings = new Map();

function getSettings(player) {
    if (!settings.has(player.id)) {
        settings.set(player.id, {
            coords: true,
            visual: false,
            fly: false
        });
    }
    return settings.get(player.id);
}

function message(player, text) {
    try {
        player.sendMessage("§a[MCPE Overlay] §f" + text);
    } catch (_) {}
}

async function openMenu(player) {
    const state = getSettings(player);

    const form = new ActionFormData()
        .title("MCPE Overlay")
        .body(
            "§7Игровое меню Add-On\n\n" +
            "§fКоординаты: " + (state.coords ? "§aВКЛ" : "§cВЫКЛ") + "\n" +
            "§fНочное зрение: " + (state.visual ? "§aВКЛ" : "§cВЫКЛ") + "\n" +
            "§fТворческий режим: " + (state.fly ? "§aВКЛ" : "§cВЫКЛ") + "\n\n" +
            "§eFPS: §7официальный Script API не предоставляет счётчик FPS.\n\n" +
            "§6Полёт включается через творческий режим. " +
            "При выключении переключение возвращает режим выживания."
        )
        .button("Координаты: " + (state.coords ? "ВЫКЛ" : "ВКЛ"))
        .button("Ночное зрение: " + (state.visual ? "ВЫКЛ" : "ВКЛ"))
        .button("Полёт / творческий режим: " + (state.fly ? "ВЫКЛ" : "ВКЛ"))
        .button("Информация о FPS")
        .button("Закрыть");

    try {
        const result = await form.show(player);

        if (result.canceled || result.selection === undefined) return;

        if (result.selection === 0) {
            state.coords = !state.coords;
            message(player, "Координаты " + (state.coords ? "включены." : "выключены."));
        } else if (result.selection === 1) {
            if (!state.visual) {
                player.addEffect("night_vision", 20 * 60 * 10, {
                    amplifier: 0,
                    showParticles: false
                });
                state.visual = true;
                message(player, "Ночное зрение включено на 10 минут.");
            } else {
                player.removeEffect("night_vision");
                state.visual = false;
                message(player, "Ночное зрение выключено.");
            }
        } else if (result.selection === 2) {
            try {
                if (!state.fly) {
                    player.runCommand("gamemode creative @s");
                    state.fly = true;
                    message(player, "Творческий режим включён. Полёт доступен в этом режиме.");
                } else {
                    player.runCommand("gamemode survival @s");
                    state.fly = false;
                    message(player, "Возвращён режим выживания.");
                }
            } catch (_) {
                message(player, "Не удалось сменить режим. Проверь разрешения и включение читов в мире.");
            }
        } else if (result.selection === 3) {
            message(player, "Счётчик FPS недоступен через официальный Script API.");
        } else {
            return;
        }

        system.runTimeout(() => openMenu(player), 2);
    } catch (_) {
        message(player, "Не удалось открыть меню. Закрой другие игровые формы и попробуй снова.");
    }
}

// Выдаём компас при первом появлении игрока.
// Используй компас, чтобы открыть игровое меню.
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

// Использование компаса открывает меню.
world.afterEvents.itemUse.subscrib
