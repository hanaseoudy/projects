package view;

import ai.AIHandler;
import multiplayer.netty.NettyPacketManager;
import view.stages.JackarooMenu;

public final class JackarooLauncher {

    public static void main(String[] args) {
        new NettyPacketManager();
        new AIHandler();
        JackarooMenu.main(args);
    }
}
