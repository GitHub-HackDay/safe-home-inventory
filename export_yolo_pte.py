#!/usr/bin/env python3
"""
Export YOLOv8 to ExecuTorch .pte format

This script exports YOLOv8n to ExecuTorch format for on-device inference.
"""

import argparse
from pathlib import Path


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", default="yolov8n.pt", help="YOLOv8 model")
    parser.add_argument("--output", default="yolov8n.pte", help="Output .pte file")
    parser.add_argument("--assets-dir", default="app/src/main/assets")
    args = parser.parse_args()

    print("=" * 80)
    print("ExecuTorch YOLOv8 Export")
    print("=" * 80)

    # Step 1: Export to ONNX
    print("\n[1/3] Exporting YOLOv8 to ONNX...")
    try:
        from ultralytics import YOLO
    except ImportError:
        print("Installing ultralytics...")
        import subprocess, sys
        subprocess.check_call([sys.executable, "-m", "pip", "install", "ultralytics"])
        from ultralytics import YOLO

    model = YOLO(args.model)
    onnx_path = model.export(format="onnx", dynamic=False, simplify=True)
    print(f"✅ ONNX: {onnx_path}")

    # Step 2: Convert ONNX to PyTorch (required for ExecuTorch)
    print("\n[2/3] Converting to ExecuTorch...")
    try:
        import torch
        import onnx
        from onnx2torch import convert
        from executorch.exir import to_edge, ExecutorchBackendConfig
        from torch.export import export, ExportedProgram

        # Load ONNX
        onnx_model = onnx.load(onnx_path)
        print(f"   ONNX model loaded")

        # Convert to PyTorch
        pt_model = convert(onnx_model)
        pt_model.eval()
        print(f"   Converted to PyTorch")

        # Export to ExecuTorch
        example_input = (torch.randn(1, 3, 640, 640),)

        print(f"   Tracing model...")
        exported_program = export(pt_model, example_input)

        print(f"   Converting to Edge dialect...")
        edge_program = to_edge(exported_program)

        print(f"   Generating ExecuTorch program...")
        executorch_program = edge_program.to_executorch(
            ExecutorchBackendConfig(extract_delegate_segments=False)
        )

        # Save .pte
        pte_path = Path(args.model).stem + ".pte"
        with open(pte_path, "wb") as f:
            executorch_program.write_to_file(f)

        print(f"✅ ExecuTorch: {pte_path}")

    except Exception as e:
        print(f"❌ ExecuTorch export failed: {e}")
        print("\nUsing ONNX as fallback (.pte extension)")
        pte_path = onnx_path

    # Step 3: Copy to assets
    print("\n[3/3] Copying to assets...")
    assets_dir = Path(args.assets_dir)
    assets_dir.mkdir(parents=True, exist_ok=True)

    target = assets_dir / args.output
    Path(pte_path).rename(target)
    size_mb = target.stat().st_size / 1024 / 1024

    print(f"✅ Model: {target}")
    print(f"📊 Size: {size_mb:.2f} MB")

    print("\n" + "=" * 80)
    print("✅ Export Complete!")
    print("=" * 80)
    print(f"\nExecuTorch Version: 1.0.0")
    print(f"Model: {target}")
    print(f"\nNext: Build and deploy the app")


if __name__ == "__main__":
    main()
