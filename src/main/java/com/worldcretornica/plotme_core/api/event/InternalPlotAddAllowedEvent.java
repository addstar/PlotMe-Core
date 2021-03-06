package com.worldcretornica.plotme_core.api.event;

import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotMe_Core;
import com.worldcretornica.plotme_core.api.IPlayer;
import com.worldcretornica.plotme_core.api.IWorld;

public class InternalPlotAddAllowedEvent extends InternalPlotEvent implements ICancellable {

    private boolean canceled;
    private final IPlayer player;
    private final String allowed;

    public InternalPlotAddAllowedEvent(PlotMe_Core instance, IWorld world, Plot plot, IPlayer player, String allowed) {
        super(instance, plot, world);
        this.player = player;
        this.allowed = allowed;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public IPlayer getPlayer() {
        return player;
    }

    public String getNewAllowed() {
        return allowed;
    }
}
