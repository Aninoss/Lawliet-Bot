package Commands.NSFW;

import CommandListeners.CommandProperties;

@CommandProperties(
        trigger = "neko",
        executable = true,
        emoji = "\uD83D\uDD1E",
        nsfw = true,
        withLoadingBar = true
)
public class NekoCommand extends Rule34ProxyCommand {
    public NekoCommand() {
        super("cat_girl", false);
    }
}