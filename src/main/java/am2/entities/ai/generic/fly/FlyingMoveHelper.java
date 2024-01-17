package am2.entities.ai.generic.fly;

import am2.entities.EntityGeneric;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;

//courtesy of CustomNPCs+
public class FlyingMoveHelper extends EntityMoveHelper{
    private final EntityGeneric entity;
    private double posX;
    private double posY;
    private double posZ;
    private double speed;
    private boolean update;

    public FlyingMoveHelper(EntityGeneric entity){
        super(entity);
        this.entity = entity;
        this.posX = entity.posX;
        this.posY = entity.posY;
        this.posZ = entity.posZ;
    }

    public void onUpdateMoveHelper() {
        if (this.update) {
            this.update = false;

            double speed = this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * Integer.parseInt(this.entity.getValue("flXZ"));
            double speedY = this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue() * Integer.parseInt(this.entity.getValue("flY"));

            double d0 = this.posX - this.entity.posX;
            double d1 = this.posY - MathHelper.floor_double(this.entity.boundingBox.minY + 0.05D);
            double d2 = this.posZ - this.entity.posZ;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            double d5 = MathHelper.sqrt_double(d4);

            double heightOffGround = 0;
            int flyLimit = Integer.parseInt(this.entity.getValue("flL"));
            if(flyLimit != -1) { // has a fly limit
                for (int blockY = (int) this.posY; blockY > 0; blockY--) {
                    heightOffGround = this.posY - blockY;
                    if (this.entity.worldObj.getBlock((int) this.posX, blockY, (int) this.posZ) != Blocks.air || heightOffGround > flyLimit) {
                        break;
                    }
                }
            }

            if (this.entity.hurtTime == 0 && d4 > 0.5D) {
                this.entity.motionX += (d0 / d5 * speed - this.entity.motionX) * speed;
                this.entity.motionZ += (d2 / d5 * speed - this.entity.motionZ) * speed;

                if (heightOffGround < flyLimit || flyLimit == -1) {
                    this.entity.motionY += (d1 / d5 * speedY - this.entity.motionY) * speedY;
                }

                this.entity.velocityChanged = true;
            }

            this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw,(float) ((Math.atan2(-d0, -d2) + Math.PI) * -(180F / Math.PI)),18.0F);
        }
    }

    private float limitAngle(float p_75639_1_, float p_75639_2_, float p_75639_3_)
    {
        float f3 = MathHelper.wrapAngleTo180_float(p_75639_2_ - p_75639_1_);

        if (f3 > p_75639_3_)
        {
            f3 = p_75639_3_;
        }

        if (f3 < -p_75639_3_)
        {
            f3 = -p_75639_3_;
        }

        return p_75639_1_ + f3;
    }

    /**
     * Sets the speed and location to move to
     */
    public void setMoveTo(double p_75642_1_, double p_75642_3_, double p_75642_5_, double p_75642_7_)
    {
        this.posX = p_75642_1_;
        this.posY = p_75642_3_;
        this.posZ = p_75642_5_;
        this.speed = p_75642_7_;
        this.update = true;
    }

    public boolean isUpdating()
    {
        return this.update;
    }

    public double getSpeed()
    {
        return this.speed;
    }
}
