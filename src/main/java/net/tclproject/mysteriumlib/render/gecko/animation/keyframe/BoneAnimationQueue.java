/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package net.tclproject.mysteriumlib.render.gecko.animation.keyframe;

import net.tclproject.mysteriumlib.render.gecko.animation.render.AnimatedModelRenderer;

public class BoneAnimationQueue
{
	public final AnimatedModelRenderer bone;
	public AnimationPointQueue rotationXQueue = new AnimationPointQueue();
	public AnimationPointQueue rotationYQueue = new AnimationPointQueue();
	public AnimationPointQueue rotationZQueue = new AnimationPointQueue();
	public AnimationPointQueue positionXQueue = new AnimationPointQueue();
	public AnimationPointQueue positionYQueue = new AnimationPointQueue();
	public AnimationPointQueue positionZQueue = new AnimationPointQueue();
	public AnimationPointQueue scaleXQueue = new AnimationPointQueue();
	public AnimationPointQueue scaleYQueue = new AnimationPointQueue();
	public AnimationPointQueue scaleZQueue = new AnimationPointQueue();

	public BoneAnimationQueue(AnimatedModelRenderer bone)
	{
		this.bone = bone;
	}
}
