package view.middleware.frontend;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.*;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnimationManager {

    public static AnimationManager INSTANCE;

    private final Map<EasingType, Interpolator> interpolators;
    private final Map<Node, ParallelTransition> activeTransitions;

    public AnimationManager() {
        INSTANCE = this;
        interpolators = new HashMap<>();
        activeTransitions = new HashMap<>();
        interpolators.put(EasingType.LINEAR, Interpolator.LINEAR);
        interpolators.put(EasingType.SINE_IN, new SineInInterpolator());
        interpolators.put(EasingType.SINE_OUT, new SineOutInterpolator());
        interpolators.put(EasingType.BACK_IN, new BackInInterpolator());
        interpolators.put(EasingType.BACK_OUT, new BackOutInterpolator());
        interpolators.put(EasingType.QUAD_IN, new QuadInInterpolator());
        interpolators.put(EasingType.QUART_IN, new QuartInInterpolator());
        interpolators.put(EasingType.QUART_OUT, new QuartOutInterpolator());
        interpolators.put(EasingType.QUINT_IN, new QuintInInterpolator());
        interpolators.put(EasingType.QUINT_OUT, new QuintOutInterpolator());
        interpolators.put(EasingType.BOUNCE_OUT, new BounceOutInterpolator());
        interpolators.put(EasingType.ELASTIC_OUT, new ElasticOutInterpolator());
        interpolators.put(EasingType.EXPONENTIAL_IN, new ExponentialInInterpolator());
        interpolators.put(EasingType.EXPONENTIAL_OUT, new ExponentialOutInterpolator());
    }

    public enum EasingType {
        LINEAR, SINE_IN, SINE_OUT, BACK_IN, BACK_OUT, QUAD_IN, QUART_IN, QUART_OUT, QUINT_IN, QUINT_OUT, BOUNCE_OUT, ELASTIC_OUT, EXPONENTIAL_IN, EXPONENTIAL_OUT
    }

    public void playToNode(final List<String> animations, final List<EasingType> easingTypes, final List<Float> fromValues, final List<Float> toValues, final Node node, final double duration, final double delay, final Runnable onFinished) {

        // Cancel any ongoing animation for this node
        if (activeTransitions.containsKey(node)) {
            activeTransitions.get(node).stop();
            activeTransitions.remove(node);
        }

        final ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.setDelay(Duration.seconds(delay));

        final int size = animations.size();
        for (int i = 0; i < size; i++) {
            final String animation = animations.get(i);
            final EasingType easingType = easingTypes.get(Math.min(i, easingTypes.size() - 1));
            final float fromValue = fromValues.get(i), toValue = toValues.get(i);
            final Interpolator interpolator = getInterpolator(easingType);

            switch (animation) {
                case "fade" -> {
                    final KeyValue keyValue = new KeyValue(node.opacityProperty(), toValue, interpolator);
                    final Timeline fadeTimeline = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), fromValue)),
                            new KeyFrame(Duration.seconds(duration), keyValue)
                    );
                    parallelTransition.getChildren().add(fadeTimeline);
                }
                case "dropShadow" -> {
                    final DropShadow dropShadow = new DropShadow();
                    dropShadow.setRadius(fromValue);
                    node.setEffect(dropShadow);
                    KeyValue keyValue = new KeyValue(dropShadow.radiusProperty(), toValue, interpolator);
                    Timeline shadowTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), keyValue));
                    parallelTransition.getChildren().add(shadowTimeline);
                }
                case "bloom" -> {
                    final Bloom bloom = new Bloom(fromValue);
                    node.setEffect(bloom);
                    final KeyValue keyValue = new KeyValue(bloom.thresholdProperty(), toValue, interpolator);
                    final Timeline bloomTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), keyValue));
                    parallelTransition.getChildren().add(bloomTimeline);
                }
                case "sepiaTone" -> {
                    final SepiaTone sepiaTone = new SepiaTone(fromValue);
                    node.setEffect(sepiaTone);
                    final KeyValue keyValue = new KeyValue(sepiaTone.levelProperty(), toValue, interpolator);
                    final Timeline sepiaTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), keyValue));
                    parallelTransition.getChildren().add(sepiaTimeline);
                }
                case "colorAdjust_brightness" -> {
                    final ColorAdjust colorAdjust = new ColorAdjust();
                    colorAdjust.setBrightness(fromValue);
                    node.setEffect(colorAdjust);
                    final KeyValue keyValue = new KeyValue(colorAdjust.brightnessProperty(), toValue, interpolator);
                    final Timeline brightnessTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), keyValue));
                    parallelTransition.getChildren().add(brightnessTimeline);
                }
                case "lighting" -> {
                    final Lighting lighting = new Lighting();
                    lighting.setSurfaceScale(fromValue);
                    node.setEffect(lighting);
                    final KeyValue keyValue = new KeyValue(lighting.surfaceScaleProperty(), toValue, interpolator);
                    final Timeline lightingTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), keyValue));
                    parallelTransition.getChildren().add(lightingTimeline);
                }
                case "innerShadow" -> {
                    final InnerShadow innerShadow = new InnerShadow();
                    innerShadow.setRadius(fromValue);
                    node.setEffect(innerShadow);
                    final KeyValue keyValue = new KeyValue(innerShadow.radiusProperty(), toValue, interpolator);
                    final Timeline innerShadowTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), keyValue));
                    parallelTransition.getChildren().add(innerShadowTimeline);
                }
                case "blur" -> {
                    final GaussianBlur gaussianBlur = new GaussianBlur(fromValue);
                    node.setEffect(gaussianBlur);
                    final KeyValue keyValue = new KeyValue(gaussianBlur.radiusProperty(), toValue, interpolator);
                    final Timeline blurTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), keyValue));
                    parallelTransition.getChildren().add(blurTimeline);
                }
                case "glow" -> {
                    final Glow glow = new Glow(fromValue);
                    node.setEffect(glow);
                    final KeyValue keyValue = new KeyValue(glow.levelProperty(), toValue, interpolator);
                    final Timeline glowTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), keyValue));
                    parallelTransition.getChildren().add(glowTimeline);
                }
                case "rotate" -> {
                    final KeyValue keyValue = new KeyValue(node.rotateProperty(), toValue, interpolator);
                    final Timeline rotateTimeline = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.rotateProperty(), fromValue)),
                            new KeyFrame(Duration.seconds(duration), keyValue)
                    );
                    parallelTransition.getChildren().add(rotateTimeline);
                }
                case "scaleX" -> {
                    final KeyValue keyValue = new KeyValue(node.scaleXProperty(), toValue, interpolator);
                    final Timeline scaleXTimeline = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.scaleXProperty(), fromValue)),
                            new KeyFrame(Duration.seconds(duration), keyValue)
                    );
                    parallelTransition.getChildren().add(scaleXTimeline);
                }
                case "scaleY" -> {
                    final KeyValue keyValue = new KeyValue(node.scaleYProperty(), toValue, interpolator);
                    final Timeline scaleYTimeline = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.scaleYProperty(), fromValue)),
                            new KeyFrame(Duration.seconds(duration), keyValue)
                    );
                    parallelTransition.getChildren().add(scaleYTimeline);
                }
                case "moveX" -> {
                    final KeyValue keyValue = new KeyValue(node.translateXProperty(), toValue, interpolator);
                    final Timeline moveXTimeline = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), fromValue)),
                            new KeyFrame(Duration.seconds(duration), keyValue)
                    );
                    parallelTransition.getChildren().add(moveXTimeline);
                }
                case "moveY" -> {
                    final KeyValue keyValue = new KeyValue(node.translateYProperty(), toValue, interpolator);
                    final Timeline moveYTimeline = new Timeline(
                            new KeyFrame(Duration.ZERO, new KeyValue(node.translateYProperty(), fromValue)),
                            new KeyFrame(Duration.seconds(duration), keyValue)
                    );
                    parallelTransition.getChildren().add(moveYTimeline);
                }
            }
        }

        parallelTransition.setOnFinished(event -> {
            activeTransitions.remove(node); // Clean up when done
            if (onFinished != null) {
                onFinished.run();
            }
        });

        activeTransitions.put(node, parallelTransition);
        parallelTransition.play();
    }

    private Interpolator getInterpolator(final EasingType easingType) {
        return interpolators.get(easingType);
    }

    // --- Custom Interpolators below (no changes needed) ---

    private static final class SineInInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            return 1 - Math.cos((t * Math.PI) / 2);
        }
    }

    private static final class SineOutInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            return Math.sin((t * Math.PI) / 2);
        }
    }

    private static final class BackInInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            final double s = 1.70158;
            return t * t * ((s + 1) * t - s);
        }
    }

    private static final class BackOutInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            final double s = 1.70158;
            final double t1 = t - 1;
            return (t1 * t1 * ((s + 1) * t1 + s) + 1);
        }
    }

    private static final class QuadInInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            return t * (2 - t);
        }
    }

    private static final class QuartInInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            return t * t * t * t;
        }
    }

    private static final class QuartOutInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            final double t1 = t - 1;
            return 1 - t1 * t1 * t1 * t1;
        }
    }

    private static final class QuintInInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            return t * t * t * t * t;
        }
    }

    private static final class QuintOutInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            final double t1 = t - 1;
            return t1 * t1 * t1 * t1 * t1 + 1;
        }
    }

    private static final class BounceOutInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            if (t < 1 / 2.75) {
                return 7.5625 * t * t;
            } else if (t < 2 / 2.75) {
                final double t1 = t - 1.5 / 2.75;
                return 7.5625 * t1 * t1 + 0.75;
            } else if (t < 2.5 / 2.75) {
                final double t1 = t - 2.25 / 2.75;
                return 7.5625 * t1 * t1 + 0.9375;
            } else {
                final double t1 = t - 2.625 / 2.75;
                return 7.5625 * t1 * t1 + 0.984375;
            }
        }
    }

    private static final class ElasticOutInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            if (t == 0) return 0;
            if (t == 1) return 1;
            return Math.pow(2, -10 * t) * Math.sin((t - 0.075) * (2 * Math.PI) / 0.3) + 1;
        }
    }

    private static final class ExponentialInInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            return t == 0 ? 0 : Math.pow(2, 10 * (t - 1));
        }
    }

    private static final class ExponentialOutInterpolator extends Interpolator {
        @Override
        protected double curve(final double t) {
            return t == 1 ? 1 : 1 - Math.pow(2, -10 * t);
        }
    }
}