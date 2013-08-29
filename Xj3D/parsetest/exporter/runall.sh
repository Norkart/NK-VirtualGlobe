CONVERTER="java -Xmx800M xj3d.converter.Xj3DConv"

rm -rf output_x3dv
mkdir output_x3dv

echo node_allfieldtypes.wrl
$CONVERTER node_allfieldtypes.wrl output_x3dv/nodeallfieldtypes.x3dv

echo browser.wrl
$CONVERTER browser.wrl output_x3dv/browser.x3dv

echo complex_proto.wrl
$CONVERTER complex_proto.wrl output_x3dv/complex_proto.x3dv

echo externproto.wrl
$CONVERTER externproto.wrl output_x3dv/externproto.x3dv

echo nested.wrl
$CONVERTER nested.wrl output_x3dv/nested.x3dv

echo proto_interface.wrl
$CONVERTER proto_interface.wrl output_x3dv/proto_interface.x3dv

echo proto_sameindex.wrl
$CONVERTER proto_sameindex.wrl output_x3dv/proto_sameindex.x3dv

echo proto_script_is.wrl
$CONVERTER proto_script_is.wrl output_x3dv/proto_script_is.x3dv

echo route_change.wrl
$CONVERTER route_change.wrl output_x3dv/route_change.x3dv

echo script_node.wrl
$CONVERTER script_node.wrl output_x3dv/script_node.x3dv

echo script_use.wrl
$CONVERTER script_use.wrl output_x3dv/script_use.x3dv

echo script_use2.x3dv
$CONVERTER script_use2.x3dv output_x3dv/script_use2.x3dv

echo upgrade_script.wrl
$CONVERTER upgrade_script.wrl output_x3dv/upgrade_script.x3dv

echo compdecl.x3dv
$CONVERTER compdecl.x3dv output_x3dv/compdecl.x3dv

echo meta.x3dv
$CONVERTER meta.x3dv output_x3dv/meta.x3dv

echo script.x3dv
$CONVERTER script.x3dv output_x3dv/script.x3dv

echo script_metadata.x3dv
$CONVERTER script_metadata.x3dv output_x3dv/script_metadata.x3dv

echo script_use.x3dv
$CONVERTER script_use.x3dv output_x3dv/script_use.x3dv

echo script_use2.x3dv
$CONVERTER script_use2.x3dv output_x3dv/script_use2.x3dv

echo script_nested.x3dv
$CONVERTER script_nested.x3dv output_x3dv/script_nested.x3dv

echo proto_import1.x3dv
$CONVERTER proto_import1.x3dv output_x3dv/proto_import1.x3dv

echo moving_box_import1.x3dv
$CONVERTER moving_box_import1.x3dv output_x3dv/moving_box_import1.x3dv

echo moving_box_export1.x3dv
$CONVERTER moving_box_export1.x3dv output_x3dv/moving_box_export1.x3dv

echo DiamondManLOA-0.wrl
$CONVERTER -upgradeContent DiamondManLOA-0.wrl output_x3dv/DiamondManLOA-0.x3dv

echo ep_use.wrl
$CONVERTER ep_use.wrl output_x3dv/ep_use.x3dv

rm -rf output_x3d
mkdir output_x3d

echo node_allfieldtypes.wrl
$CONVERTER node_allfieldtypes.wrl output_x3d/nodeallfieldtypes.x3d

echo browser.wrl
$CONVERTER browser.wrl output_x3d/browser.x3d

echo complex_proto.wrl
$CONVERTER complex_proto.wrl output_x3d/complex_proto.x3d

echo externproto.wrl
$CONVERTER externproto.wrl output_x3d/externproto.x3d

echo nested.wrl
$CONVERTER nested.wrl output_x3d/nested.x3d

echo proto_interface.wrl
$CONVERTER proto_interface.wrl output_x3d/proto_interface.x3d

echo proto_sameindex.wrl
$CONVERTER proto_sameindex.wrl output_x3d/proto_sameindex.x3d

echo proto_script_is.wrl
$CONVERTER proto_script_is.wrl output_x3d/proto_script_is.x3d

echo route_change.wrl
$CONVERTER route_change.wrl output_x3d/route_change.x3d

echo script_node.wrl
$CONVERTER script_node.wrl output_x3d/script_node.x3d

echo script_use.wrl
$CONVERTER script_use.wrl output_x3d/script_use.x3d

echo script_use2.x3dv
$CONVERTER script_use2.x3dv output_x3d/script_use2.x3d

echo script_nested.x3dv
$CONVERTER script_nested.x3dv output_x3d/script_nested.x3d

echo upgrade_script.wrl
$CONVERTER upgrade_script.wrl output_x3d/upgrade_script.x3d

echo compdecl.x3dv
$CONVERTER compdecl.x3dv output_x3d/compdecl.x3d

echo meta.x3dv
$CONVERTER meta.x3dv output_x3d/meta.x3d

echo script.x3dv
$CONVERTER script.x3dv output_x3d/script.x3d

echo script_metadata.x3dv
$CONVERTER script_metadata.x3dv output_x3d/script_metadata.x3d

echo script_use.x3dv
$CONVERTER script_use.x3dv output_x3d/script_use.x3d

echo script_use2.x3dv
$CONVERTER script_use2.x3dv output_x3d/script_use2.x3d

echo proto_import1.x3dv
$CONVERTER proto_import1.x3dv output_x3d/proto_import1.x3d

echo moving_box_import1.x3dv
$CONVERTER moving_box_import1.x3dv output_x3d/moving_box_import1.x3d

echo moving_box_export1.x3dv
$CONVERTER moving_box_export1.x3dv output_x3d/moving_box_export1.x3d

# Changing encodings does not change url reference
cp moving_box_export1.x3dv output_x3d

echo containerfield.wrl
$CONVERTER containerfield.wrl output_x3d/containerfield.x3d

echo DiamondManLOA-0.wrl
$CONVERTER -upgradeContent DiamondManLOA-0.wrl output_x3d/DiamondManLOA-0.x3d

echo xml_escaping.wrl
$CONVERTER xml_escaping.wrl output_x3d/xml_escaping.x3d

echo ep_use.wrl
$CONVERTER ep_use.wrl output_x3d/ep_use.x3d


rm -rf output_x3db
mkdir output_x3db

echo node_allfieldtypes.wrl
$CONVERTER node_allfieldtypes.wrl output_x3db/nodeallfieldtypes.x3db

echo browser.wrl
$CONVERTER browser.wrl output_x3db/browser.x3db

echo complex_proto.wrl
$CONVERTER complex_proto.wrl output_x3db/complex_proto.x3db

echo externproto.wrl
$CONVERTER externproto.wrl output_x3db/externproto.x3db

echo nested.wrl
$CONVERTER nested.wrl output_x3db/nested.x3db

echo proto_interface.wrl
$CONVERTER proto_interface.wrl output_x3db/proto_interface.x3db

echo proto_sameindex.wrl
$CONVERTER proto_sameindex.wrl output_x3db/proto_sameindex.x3db

echo proto_script_is.wrl
$CONVERTER proto_script_is.wrl output_x3db/proto_script_is.x3db

echo route_change.wrl
$CONVERTER route_change.wrl output_x3db/route_change.x3db

echo script_node.wrl
$CONVERTER script_node.wrl output_x3db/script_node.x3db

echo script_use.wrl
$CONVERTER script_use.wrl output_x3db/script_use.x3db

echo script_use2.x3dv
$CONVERTER script_use2.x3dv output_x3db/script_use2.x3db

echo script_nested.x3dv
$CONVERTER script_nested.x3dv output_x3db/script_nested.x3db

echo upgrade_script.wrl
$CONVERTER upgrade_script.wrl output_x3db/upgrade_script.x3db

echo compdecl.x3dv
$CONVERTER compdecl.x3dv output_x3db/compdecl.x3db

echo meta.x3dv
$CONVERTER meta.x3dv output_x3db/meta.x3db

echo script.x3dv
$CONVERTER script.x3dv output_x3db/script.x3db

echo script_metadata.x3dv
$CONVERTER script_metadata.x3dv output_x3db/script_metadata.x3db

echo script_use.x3dv
$CONVERTER script_use.x3dv output_x3db/script_use.x3db

echo script_use2.x3dv
$CONVERTER script_use2.x3dv output_x3db/script_use2.x3db

echo proto_import1.x3dv
$CONVERTER proto_import1.x3dv output_x3db/proto_import1.x3db

echo moving_box_import1.x3dv
$CONVERTER moving_box_import1.x3dv output_x3db/moving_box_import1.x3db

echo moving_box_export1.x3dv
$CONVERTER moving_box_export1.x3dv output_x3db/moving_box_export1.x3db

# Changing encodings does not change url reference
cp moving_box_export1.x3dv output_x3db

echo containerfield.wrl
$CONVERTER containerfield.wrl output_x3db/containerfield.x3db

echo DiamondManLOA-0.wrl
$CONVERTER -upgradeContent DiamondManLOA-0.wrl output_x3db/DiamondManLOA-0.x3db

echo xml_escaping.wrl
$CONVERTER xml_escaping.wrl output_x3db/xml_escaping.x3db

echo ep_use.wrl
$CONVERTER ep_use.wrl output_x3db/ep_use.x3db

