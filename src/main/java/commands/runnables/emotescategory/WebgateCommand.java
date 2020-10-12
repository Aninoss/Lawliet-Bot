package commands.runnables.emotescategory;
import commands.listeners.CommandProperties;
import commands.runnables.EmoteAbstract;

import java.util.Locale;

@CommandProperties(
        trigger = "webgate",
        emoji = "⛩️",
        executableWithoutArgs = true,
        exlusiveServers = { 580048842020487180L }
)
public class WebgateCommand extends EmoteAbstract {

    public WebgateCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    protected String[] getGifs() {
        return new String[]{
                "https://media.discordapp.net/attachments/745660306239258767/745660330562027540/1ae440510ab43961363838d69fa16aa3.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660333263159396/2e2ee82e.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660336215949412/3kpd5a.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660338199986206/3kpdmr.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660341173616690/3kpeyz.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660342029385779/3vw2gr.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660345892208680/3x54xx.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660348861775973/3x56mo.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660350019534860/3x57c5.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660352452231238/3x570z.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660376162500689/3xd3yn.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660380079980655/3x578k.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660385365065738/9k.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660388472782988/31b2d0b97ea98fd0dec090b705b134204925fa4c_hq.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660396031049728/543.gif",
                "https://media.discordapp.net/attachments/745660306239258767/745660397205323916/121e614c9fe09fd6.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660402997657760/2020-08-08_21.29.44.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660405191540776/948340_1380835166183_full.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660408605704292/20191016_160541.563.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660412137308200/20200804_012047.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660423659061380/20200805_131407.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660427085676654/20200806_180715.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660429514309722/20200812_113018.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660436258488431/76713973_2294065060885476_6630992590885879808_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660436350894110/20200814_143858.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660440700387428/91284460_588060841793186_7226083731243333300_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660443468496926/91342006_598867314031768_2970398708184123932_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660446358372412/91469241_524358461598536_4362652752199163547_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660447474057318/93529756_241650327219306_5911496248171138066_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660450536161340/94196293_259091105125162_1331837438136228170_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660467472760967/113261352_569395307086326_8551146570039075680_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660479678185603/aaa.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660484073816064/afaf_2.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660489132015666/afaf.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660490319134770/afafaf.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660498468405268/Anime-Restaurants-and-Bars-Food-Wars.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660498728583308/anime-christmas-2018-wallpaper-3200x2400-12477_29.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660503711416391/Asche_meme.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660506957807686/Bambi.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660511450038303/besser_2.0.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660518806716539/CEO_Lounge.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660520220065882/Corbi_Duncan_Vogue.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660524032819410/fesgfwafwawfa.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660531314131024/gagag.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660536313872495/ghhh.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660539669184563/graveyard.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660544530251877/gwg.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660547449618533/image0.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660526566309968/fjfjfjf.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660559290138764/IMG_20200808_232743.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660565778595981/Junior_Moderator_Snickers.PNG",
                "https://media.discordapp.net/attachments/745660306239258767/745660572309258373/Kablaya.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660562041602209/jueuji.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660576075612200/kingdeku.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660578873213068/konachan-com-36308-aqua_hair-blue_hair-bow-brown_hair-console-green_hair-group-headphones-kaito-long.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660584225275914/MV5BOWJiNjllZGEtYzIxOS00ZGFhLTgwNWMtNzNjNTM4NzJhM2RkXkEyXkFqcGdeQXVyMzgxODM4NjM._V1_UY1200_CR8506301.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660586356113458/nom.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660600520147033/PicsArt_08-05-01.04.28.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660601103155220/p4v1mrcek8q11.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660606815797278/PicsArt_08-05-02.50.19.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660611974922254/PicsArt_08-07-12.28.02.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660614600425563/PSX_20200805_145831.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660618513842216/rage.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660623202943147/rjrj.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660626000674837/scout_mains.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660630236659742/Screenshot_20200804_010820.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660635185938502/Screenshot_20200811_231159.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660650268786688/sgsg.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660652772655204/shdj.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660657504092162/sztgs.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660662767812708/test.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660669982146580/tkmfk.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660671902875668/Tuan_goes_brrrr.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660675141009438/uffff.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660682996940860/Unbenannt-1.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660684875989012/Unbenannt.pngrewg.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660687388508261/unknown.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660698671054920/VOCALOID.full.1505695.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660701753737216/WelcomeToTheNHK1.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660707688677436/wgwg.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745660713531605183/Z.png",
                "https://media.discordapp.net/attachments/745660306239258767/745660715397808198/zz.png",
                "https://media.discordapp.net/attachments/745660306239258767/745661424252223548/20200804_233650.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745661431307042908/95722845_240025797107687_5944562684228763072_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745661437619339404/106233059_673104176577462_1614223870327634262_n.jpg",
                "https://media.discordapp.net/attachments/745660306239258767/745661443919052810/unnamed.png"
        };
    }

}
