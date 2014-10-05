package com.worldcretornica.plotme_core.commands;

import com.worldcretornica.plotme_core.Plot;
import com.worldcretornica.plotme_core.PlotMapInfo;
import com.worldcretornica.plotme_core.PlotMe_Core;
import com.worldcretornica.plotme_core.api.IOfflinePlayer;
import com.worldcretornica.plotme_core.api.IPlayer;
import com.worldcretornica.plotme_core.api.IWorld;
import com.worldcretornica.plotme_core.api.event.InternalPlotAuctionEvent;
import net.milkbowl.vault.economy.EconomyResponse;

public class CmdAuction extends PlotCommand {

    public CmdAuction(PlotMe_Core instance) {
        super(instance);
    }

    public boolean exec(IPlayer p, String[] args) {
        if (plugin.getPlotMeCoreManager().isEconomyEnabled(p)) {
            PlotMapInfo pmi = plugin.getPlotMeCoreManager().getMap(p);

            if (pmi.isCanPutOnSale()) {
                if (plugin.cPerms(p, "PlotMe.use.auction") || plugin.cPerms(p, "PlotMe.admin.auction")) {
                    String id = plugin.getPlotMeCoreManager().getPlotId(p.getLocation());

                    if (id.isEmpty()) {
                        p.sendMessage(RED + C("MsgNoPlotFound"));
                    } else if (!plugin.getPlotMeCoreManager().isPlotAvailable(id, p)) {
                        Plot plot = plugin.getPlotMeCoreManager().getPlotById(p, id);

                        String name = p.getName();

                        if (plot.getOwner().equalsIgnoreCase(name) || plugin.cPerms(p, "PlotMe.admin.auction")) {
                            IWorld w = p.getWorld();

                            if (plot.isAuctionned()) {
                                if (plot.getCurrentBidderId() != null && !plugin.cPerms(p, "PlotMe.admin.auction")) {
                                    p.sendMessage(RED + C("MsgPlotHasBidsAskAdmin"));
                                } else {
                                    if (plot.getCurrentBidderId() != null) {
                                        IOfflinePlayer playercurrentbidder = sob.getOfflinePlayer(plot.getCurrentBidderId());
                                        EconomyResponse er = sob.depositPlayer(playercurrentbidder, plot.getCurrentBid());

                                        if (!er.transactionSuccess()) {
                                            p.sendMessage(RED + er.errorMessage);
                                            plugin.getUtil().warn(er.errorMessage);
                                        } else {
                                            for (IPlayer player : sob.getOnlinePlayers()) {
                                                if (player.getName().equalsIgnoreCase(plot.getCurrentBidder())) {
                                                    player.sendMessage(C("MsgAuctionCancelledOnPlot")
                                                                               + " " + id + " " + C("MsgOwnedBy") + " " + plot.getOwner() + ". " + plugin.getUtil().moneyFormat(plot.getCurrentBid()));
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    plot.setAuctionned(false);
                                    plugin.getPlotMeCoreManager().adjustWall(p.getLocation());
                                    plugin.getPlotMeCoreManager().setSellSign(w, plot);
                                    plot.setCurrentBid(0);
                                    plot.setCurrentBidder("");

                                    plot.updateField("currentbid", 0);
                                    plot.updateField("currentbidder", "");
                                    plot.updateField("currentbidderid", null);
                                    plot.updateField("auctionned", false);

                                    p.sendMessage(C("MsgAuctionCancelled"));

                                    if (isAdvancedLogging()) {
                                        plugin.getLogger().info(LOG + name + " " + C("MsgStoppedTheAuctionOnPlot") + " " + id);
                                    }
                                }
                            } else {
                                double bid = 1;

                                if (args.length == 2) {
                                    try {
                                        bid = Double.parseDouble(args[1]);
                                    } catch (NumberFormatException e) {
                                    }
                                }

                                if (bid < 0) {
                                    p.sendMessage(RED + C("MsgInvalidAmount"));
                                } else {

                                    InternalPlotAuctionEvent event = sob.getEventFactory().callPlotAuctionEvent(plugin, w, plot, p, bid);

                                    if (!event.isCancelled()) {
                                        plot.setCurrentBid(bid);
                                        plot.setAuctionned(true);
                                        plugin.getPlotMeCoreManager().adjustWall(p.getLocation());
                                        plugin.getPlotMeCoreManager().setSellSign(w, plot);

                                        plot.updateField("currentbid", bid);
                                        plot.updateField("auctionned", true);

                                        p.sendMessage(C("MsgAuctionStarted"));

                                        if (isAdvancedLogging()) {
                                            plugin.getLogger().info(LOG + name + " " + C("MsgStartedAuctionOnPlot") + " " + id + " " + C("WordAt") + " " + bid);
                                        }
                                    }
                                }
                            }
                        } else {
                            p.sendMessage(RED + C("MsgDoNotOwnPlot"));
                        }
                    } else {
                        p.sendMessage(RED + C("MsgThisPlot") + "(" + id + ") " + C("MsgHasNoOwner"));
                    }
                } else {
                    p.sendMessage(RED + C("MsgPermissionDenied"));
                }
            } else {
                p.sendMessage(RED + C("MsgSellingPlotsIsDisabledWorld"));
            }
        } else {
            p.sendMessage(RED + C("MsgEconomyDisabledWorld"));
        }
        return true;
    }
}
