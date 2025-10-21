#!/usr/bin/env python3
"""
Export YOLOv8 model to ExecuTorch format for on-device inference.

Usage:
    python export_yolo_executorch.py

This will download YOLOv8n and export it to .pte format for Android.
"""

import argparse
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(description="Export YOLOv8 to ExecuTorch")
    parser.add_argument(
        "--model",
        type=str,
        default="yolov8n.pt",
        help="YOLOv8 model variant (default: yolov8n.pt)",
    )
    parser.add_argument(
        "--output",
        type=str,
        default="yolov8n.onnx",
        help="Output .onnx filename (default: yolov8n.onnx)",
    )
    parser.add_argument(
        "--assets-dir",
        type=str,
        default="app/src/main/assets",
        help="Android assets directory",
    )
    args = parser.parse_args()

    try:
        from ultralytics import YOLO
    except ImportError:
        print("❌ ultralytics not installed. Installing...")
        import subprocess
        import sys

        subprocess.check_call([sys.executable, "-m", "pip", "install", "ultralytics"])
        from ultralytics import YOLO

    print(f"📦 Loading YOLOv8 model: {args.model}")
    model = YOLO(args.model)

    print(f"🔄 Exporting to ONNX format (optimized for mobile)...")
    # Export to ONNX - widely supported on Android with NNAPI/GPU acceleration
    model.export(format="onnx", dynamic=False, simplify=True)

    # Move to assets directory
    assets_dir = Path(args.assets_dir)
    assets_dir.mkdir(parents=True, exist_ok=True)

    # The export creates modelname.onnx in the current directory
    model_name = Path(args.model).stem
    pte_file = Path(f"{model_name}.onnx")

    if pte_file.exists():
        target_file = assets_dir / args.output
        pte_file.rename(target_file)
        print(f"✅ Model exported to: {target_file}")
        print(f"📊 File size: {target_file.stat().st_size / 1024 / 1024:.2f} MB")
    else:
        print(f"❌ Export failed - .onnx file not found")
        return

    # Also copy the labels file
    print(f"\n📝 Creating COCO labels file...")
    create_coco_labels(assets_dir / "coco_labels.txt")

    print("\n✅ Export complete!")
    print(f"\nNext steps:")
    print(f"1. The model is ready at: {assets_dir / args.output}")
    print(f"2. Update ExecuTorchDetector.kt to load this model")
    print(f"3. Build and run the app")


def create_coco_labels(output_path: Path):
    """Create COCO class labels file."""
    # COCO 80 classes
    coco_classes = [
        "person",
        "bicycle",
        "car",
        "motorcycle",
        "airplane",
        "bus",
        "train",
        "truck",
        "boat",
        "traffic light",
        "fire hydrant",
        "stop sign",
        "parking meter",
        "bench",
        "bird",
        "cat",
        "dog",
        "horse",
        "sheep",
        "cow",
        "elephant",
        "bear",
        "zebra",
        "giraffe",
        "backpack",
        "umbrella",
        "handbag",
        "tie",
        "suitcase",
        "frisbee",
        "skis",
        "snowboard",
        "sports ball",
        "kite",
        "baseball bat",
        "baseball glove",
        "skateboard",
        "surfboard",
        "tennis racket",
        "bottle",
        "wine glass",
        "cup",
        "fork",
        "knife",
        "spoon",
        "bowl",
        "banana",
        "apple",
        "sandwich",
        "orange",
        "broccoli",
        "carrot",
        "hot dog",
        "pizza",
        "donut",
        "cake",
        "chair",
        "couch",
        "potted plant",
        "bed",
        "dining table",
        "toilet",
        "tv",
        "laptop",
        "mouse",
        "remote",
        "keyboard",
        "cell phone",
        "microwave",
        "oven",
        "toaster",
        "sink",
        "refrigerator",
        "book",
        "clock",
        "vase",
        "scissors",
        "teddy bear",
        "hair drier",
        "toothbrush",
    ]

    with open(output_path, "w") as f:
        f.write("\n".join(coco_classes))

    print(f"✅ Labels saved to: {output_path}")


if __name__ == "__main__":
    main()
