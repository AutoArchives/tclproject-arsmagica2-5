package net.tclproject.mysteriumlib.render.gecko.iceandfire.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModelHandler;

@SideOnly(Side.CLIENT)
public enum EnumDragonAnimations {
   MALE("BaseMale"),
   FEMALE("BaseFemale"),
   FLYING_POSE("Flying"),
   DIVING_POSE("Diving"),
   GROUND_POSE("Ground"),
   HOVERING_POSE("Hovering"),
   SITTING_POSE("Sitting"),
   SLEEPING_POSE("Sleeping"),
   SWIM_POSE("Swimming", 2),
   SWIM1("Swim1", 2),
   SWIM2("Swim2", 2),
   SWIM3("Swim3", 2),
   SWIM4("Swim4", 2),
   SWIM5("Swim5", 2),
   BITE1("Bite1"),
   BITE2("Bite2"),
   BITE3("Bite3"),
   BLAST_BREATH("AttackBlastBreath"),
   BLAST_CHARGE1("AttackBlastCharge1"),
   BLAST_CHARGE2("AttackBlastCharge2"),
   BLAST_CHARGE3("AttackBlastCharge3"),
   STREAM_BREATH("AttackStreamBreath"),
   STREAM_CHARGE1("AttackStreamCharge1"),
   STREAM_CHARGE2("AttackStreamCharge2"),
   STREAM_CHARGE3("AttackStreamCharge3"),
   DEAD("Dead"),
   GRAB1("Grab1"),
   GRAB2("Grab2"),
   GRAB_SHAKE1("GrabShake1"),
   GRAB_SHAKE2("GrabShake2"),
   GRAB_SHAKE3("GrabShake3"),
   ROAR1("Roar1"),
   ROAR2("Roar2"),
   ROAR3("Roar3"),
   EPIC_ROAR1("EpicRoar1"),
   EPIC_ROAR2("EpicRoar2"),
   EPIC_ROAR3("EpicRoar3"),
   TAIL_WHIP1("Tail1"),
   TAIL_WHIP2("Tail2"),
   TAIL_WHIP3("Tail3"),
   WING_BLAST1("WingBlast1"),
   WING_BLAST2("WingBlast2"),
   WING_BLAST3("WingBlast3"),
   WING_BLAST4("WingBlast4"),
   WING_BLAST5("WingBlast5"),
   WING_BLAST6("WingBlast6"),
   WING_BLAST7("WingBlast7"),
   WALK1("Walk1", 1),
   WALK2("Walk2", 1),
   WALK3("Walk3", 1),
   WALK4("Walk4", 1),
   FLIGHT1("Flight1"),
   FLIGHT2("Flight2"),
   FLIGHT3("Flight3"),
   FLIGHT4("Flight4"),
   FLIGHT5("Flight5"),
   FLIGHT6("Flight6"),
   TACKLE("AttackTackle"),
   SIT_ON_PLAYER_POSE("SittingShoulder", 1);

   @SideOnly(Side.CLIENT)
   public IceAndFireTabulaModel firedragon_model;
   @SideOnly(Side.CLIENT)
   public IceAndFireTabulaModel icedragon_model;
   private String fileSuffix;
   private int dragonType;

   private EnumDragonAnimations(String fileSuffix) {
      this.fileSuffix = fileSuffix;
   }

   private EnumDragonAnimations(String fileSuffix, int iceOrFire) {
      this.fileSuffix = fileSuffix;
      this.dragonType = iceOrFire;
   }

   @SideOnly(Side.CLIENT)
   public static void initializeDragonModels() {
      if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
         EnumDragonAnimations[] var0 = values();
         int var1 = var0.length;

         for(int var2 = 0; var2 < var1; ++var2) {
            EnumDragonAnimations animation = var0[var2];
            switch(animation.dragonType) {
            case 0:
               try {
                  animation.firedragon_model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/firedragon/dragonFire" + animation.fileSuffix));
               } catch (Exception var8) {
                  var8.printStackTrace();
               }

               try {
                  animation.icedragon_model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/icedragon/dragonIce" + animation.fileSuffix));
               } catch (Exception var7) {
                  var7.printStackTrace();
               }
               break;
            case 1:
               try {
                  animation.firedragon_model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/firedragon/dragonFire" + animation.fileSuffix));
                  animation.icedragon_model = animation.firedragon_model;
               } catch (Exception var6) {
                  var6.printStackTrace();
               }
               break;
            case 2:
               try {
                  animation.icedragon_model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/icedragon/dragonIce" + animation.fileSuffix));
                  animation.firedragon_model = animation.icedragon_model;
               } catch (Exception var5) {
                  var5.printStackTrace();
               }
            }
         }
      }

   }
}
