package kanin.rightclickharvest

import com.gmail.nossr50.api.ExperienceAPI
import com.gmail.nossr50.datatypes.skills.PrimarySkillType
import com.gmail.nossr50.util.player.UserManager
import kr.entree.spigradle.annotations.PluginMain
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.Directional
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin


@PluginMain
class RightClickHarvest : JavaPlugin(), Listener {
    override fun onEnable() {
        logger.info("RightClickHarvest has loaded")
        Bukkit.getPluginManager().registerEvents(this, this)
    }

    @EventHandler
    fun rightClick(e: PlayerInteractEvent) {
        val p = e.player
        if (e.action == Action.RIGHT_CLICK_BLOCK) {
            val b: Block = e.clickedBlock!!
            if (!checkForCrop(b.type) && !checkForCoca(b.type)) {
                return
            }
            if (checkForCrop(b.type) && !checkForTool(p, "hoe")) {
                return
            } else if (checkForCoca(b.type) && !checkForTool(p, "axe")) {
                return
            }
            val ageable: Ageable = b.blockData as Ageable
            val actualAge: Int = ageable.age
            if (actualAge == ageable.maximumAge) {
                harvest(b, p)
                if (checkForMcMMO()) {
                    val blockState = ArrayList<BlockState>()
                    blockState.add(b.state)
                    ExperienceAPI.addXpFromBlocksBySkill(
                        blockState,
                        UserManager.getPlayer(p),
                        PrimarySkillType.getSkill("Herbalism")
                    )
                }
            }
        }
    }

    private fun harvest(b: Block, p: Player) {
        val setToBlock = b.type
        p.swingMainHand()
        changeOutputAndBreak(b, p)
        b.type = setToBlock
        changeItemDurability(p)
        changeCocaDirection(b)
        playSound(p, b)
    }

    private fun changeOutputAndBreak(b: Block, p: Player) {
        val location: Location = b.location
        val blockDrops: Collection<ItemStack> = b.getDrops(p.inventory.itemInMainHand)
        val blockDropItems: Array<Any> = blockDrops.toTypedArray()
        for (i in blockDropItems.indices) {
            val item: ItemStack = blockDropItems[i] as ItemStack
            if (item.type === getSeed(b.type)) {
                item.amount = item.amount - 1
            }
            if (item.amount != 0) {
                location.world?.dropItemNaturally(location, item)
            }
        }
    }

    private fun getSeed(m: Material): Material {
        return when (m) {
            Material.WHEAT -> Material.WHEAT_SEEDS
            Material.POTATOES -> Material.POTATO
            Material.CARROTS -> Material.CARROT
            Material.BEETROOTS -> Material.BEETROOT_SEEDS
            Material.NETHER_WART -> Material.NETHER_WART
            Material.COCOA -> Material.COCOA_BEANS
            else -> Material.AIR
        }
    }

    private fun changeItemDurability(p: Player) {
        val item = p.inventory.itemInMainHand
        if (item.itemMeta!!.isUnbreakable) {
            return
        }
        if (p.gameMode != GameMode.CREATIVE) {
            val itemDamage: Damageable? = item.itemMeta as Damageable?
            val damage: Int? = itemDamage?.damage?.plus(1)
            if (damage != null) {
                itemDamage.damage = damage.toShort().toInt()
            }
            item.itemMeta = itemDamage
        }
    }

    private fun changeCocaDirection(b: Block) {
        if (checkForCoca(b.type)) {
            val blockData = b.blockData
            val cocoaBean = b.location
            val cocoaBeanX = cocoaBean.x
            val cocoaBeanZ = cocoaBean.z
            cocoaBean.z = cocoaBeanZ - 1
            if (checkForJungleLog(cocoaBean.block.type)) {
                return
            }
            cocoaBean.z = cocoaBeanZ + 1
            if (checkForJungleLog(cocoaBean.block.type)) {
                (blockData as Directional).facing = BlockFace.SOUTH
                b.blockData = blockData
                return
            }
            cocoaBean.x = cocoaBeanX - 1
            cocoaBean.z = cocoaBeanZ
            if (checkForJungleLog(cocoaBean.block.type)) {
                (blockData as Directional).facing = BlockFace.WEST
                b.blockData = blockData
                return
            }
            cocoaBean.x = cocoaBeanX + 1
            cocoaBean.z = cocoaBeanZ
            if (checkForJungleLog(cocoaBean.block.type)) {
                (blockData as Directional).facing = BlockFace.EAST
                b.blockData = blockData
                return
            }
        }
    }

    private fun checkForJungleLog(m: Material): Boolean {
        return m == Material.JUNGLE_LOG || m == Material.STRIPPED_JUNGLE_LOG
    }

    private fun playSound(p: Player, b: Block) {
        val m: Material = b.type
        if (m == Material.NETHER_WART) {
            p.playSound(b.location, Sound.BLOCK_NETHER_WART_BREAK, 10f, 1f)
            p.playSound(b.location, Sound.ITEM_NETHER_WART_PLANT, 8f, 1f)
        } else {
            p.playSound(b.location, Sound.BLOCK_CROP_BREAK, 10f, 1f)
            p.playSound(b.location, Sound.ITEM_CROP_PLANT, 8f, 1f)
        }
    }

    private fun checkForCrop(m: Material): Boolean {
        return m == Material.WHEAT || m == Material.POTATOES || m == Material.CARROTS || m == Material.BEETROOTS || m == Material.NETHER_WART
    }

    private fun checkForCoca(m: Material): Boolean {
        return m == Material.COCOA
    }

    private fun checkForTool(p: Player, t: String): Boolean {
        return when (t) {
            "axe" -> {
                return when (p.inventory.itemInMainHand.type) {
                    Material.NETHERITE_AXE, Material.DIAMOND_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.STONE_AXE, Material.WOODEN_AXE -> true
                    else -> false
                }
            }
            "hoe" -> {
                return when (p.inventory.itemInMainHand.type) {
                    Material.NETHERITE_HOE, Material.DIAMOND_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.STONE_HOE, Material.WOODEN_HOE -> true
                    else -> false
                }
            }
            else -> false
        }
    }

    private fun checkForMcMMO(): Boolean {
        val mcmmo: Plugin? = server.pluginManager.getPlugin("mcMMO")
        return mcmmo != null && mcmmo.isEnabled
    }
}

//private fun <E> Collection<E>.toArray(arrayOfNulls: Array<Any?>): Array<Any> {
//
//}
