package commands.runnables.nsfwinteractionscategory;

import commands.listeners.CommandProperties;
import commands.runnables.RolePlayAbstract;

import java.util.Locale;

@CommandProperties(
        trigger = "fuck",
        emoji = "\uD83D\uDECF️",
        executableWithoutArgs = true,
        nsfw = true,
        requiresFullMemberCache = true
)
public class FuckCommand extends RolePlayAbstract {

    public FuckCommand(Locale locale, String prefix) {
        super(locale, prefix, true, true,
                "https://cdn.discordapp.com/attachments/736281485216317442/736281490484363344/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281495257219112/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281501951590430/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281505260765235/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281509488754740/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281518988853358/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281529122029628/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281556859224074/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281570234728488/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281590858252368/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281600660209695/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281606838550562/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281611712331796/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281619572195378/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281724828254241/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281732835180565/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281739852382308/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281746059952168/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281750543532126/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281763680092190/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281774547665016/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281794927919164/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281798841204846/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281806084767861/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281817845596320/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281826154250270/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281834740252833/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281838242496602/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281848426135582/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281854746951870/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281859821928529/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281866805706752/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281876154810408/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281881833897984/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281887823233105/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281899454169198/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281906164924526/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281915144929311/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281920832405624/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281925446139924/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736281929590243489/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736282288844963891/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/736282318788100157/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504681929441340/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504694314434641/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504706431647754/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504717995999282/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504729135546428/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504740397907968/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504751953346620/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504764301639770/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504779337564220/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504793681821756/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504805186666517/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504819333922876/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504831435014174/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504843614748672/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504856135794739/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504868944412682/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504880298524702/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504891498102814/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504903023657000/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504915186483252/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504927405015120/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504941166395513/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504957121789993/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834504993163444294/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505557791735898/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505568831144016/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505579565285417/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505592860704788/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505605435490414/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505616780427324/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505628352512010/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505640805531668/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505654043017216/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505669088509970/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505681498800138/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505696023150662/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505710149959710/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505729607335987/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505743930753084/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505758375936021/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505769625976873/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505780685963354/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505792626753566/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505805549273098/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505825778270329/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505841902223452/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505854895259678/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505876944453692/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505937364320316/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505950391566376/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505963209621514/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505974165405766/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834505987490578529/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834506000609837096/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834506018188165160/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/834506032612114492/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/839471780678991882/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/839471858193530910/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881903289946484776/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881903723922731129/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881903786346549259/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881903844748046406/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881903908941873172/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881903968723275786/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881904031323287592/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881904123405021264/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/881904190358704148/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/1130880574412828672/fuck.gif",
                "https://cdn.discordapp.com/attachments/736281485216317442/1220286272581668864/fuck.gif"
        );
    }

}
