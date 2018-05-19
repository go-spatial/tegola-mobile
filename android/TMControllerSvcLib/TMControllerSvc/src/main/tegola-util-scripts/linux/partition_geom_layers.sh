#!/usr/bin/env bash

# Usage: partition_geom_layers
#   [-pname <provider name for tlayer==provider>]
#   -gname <geom/table name>
#   -idfieldname <id field name in geom table>
#   -geomfieldname <geom type field name in geom table>
#   -gsize <recordset size of geom>
#   -psize <recordset size for each parition of geom>
#   [-zmin <min zoom for tlayer==map>]
#   [-zmax <max zoom for tlayer==map>]
#

#error codes
ERR__NO_ARGS=-1
ERR__INVALID_ARG=-2
ERR__MISSING_REQ_ARG=-3

while [[ $# -gt 0 ]] ; do
    argument_name="$1"
    argument_value="$2"

    case $argument_name in
        -pname)
            PNAME=$argument_value
            ;;
        -zmin)
            ZMIN=$argument_value
            ;;
        -zmax)
            ZMAX=$argument_value
            ;;
        -gname)
            GNAME=$argument_value
            ;;
        -idfieldname)
            IDFIELDNAME=$argument_value
            ;;
        -geomfieldname)
            GEOMFIELDNAME=$argument_value
            ;;
        -gsize)
            GSIZE=$argument_value
            ;;
        -psize)
            PSIZE=$argument_value
            ;;
        *)  # unknown argument
            echo partition_geom_layers.sh: ERROR: invalid argument "$argument_name"
            exit $ERR__INVALID_ARG
            ;;
    esac

    shift
    shift
done

if [[ -z "${PNAME}" ]]; then
    echo partition_geom_layers.sh: ERROR: missing required argument: pname \for tlayer==provider
    exit $ERR__MISSING_REQ_ARG
fi
if [[ -z "${GNAME}" ]]; then
    echo partition_geom_layers.sh: ERROR: missing required argument: gname
    exit $ERR__MISSING_REQ_ARG
fi
if [[ -z "${IDFIELDNAME}" ]]; then
    echo partition_geom_layers.sh: ERROR: missing required argument: idfieldname
    exit $ERR__MISSING_REQ_ARG
fi
if [[ -z "${GEOMFIELDNAME}" ]]; then
    echo partition_geom_layers.sh: ERROR: missing required argument: geomfieldname
    exit $ERR__MISSING_REQ_ARG
fi
if [[ -z "${GSIZE}" ]]; then
    echo partition_geom_layers.sh: ERROR: missing required argument: gsize
    exit $ERR__MISSING_REQ_ARG
fi
if [[ -z "${PSIZE}" ]]; then
    echo partition_geom_layers.sh: ERROR: missing required argument: psize
    exit $ERR__MISSING_REQ_ARG
fi
if [[ -z "${ZMIN}" ]]; then
    echo partition_geom_layers.sh: ERROR: missing required argument: zmin \for tlayer==map
    exit $ERR__MISSING_REQ_ARG
fi
if [[ -z "${ZMAX}" ]]; then
    echo partition_geom_layers.sh: ERROR: missing required argument: zmax \for tlayer==map
    exit $ERR__MISSING_REQ_ARG
fi

REMAINDER=$(( $GSIZE % $PSIZE ))
N_WHOLE_PARTITIONS=$(( ($GSIZE - $REMAINDER) / $PSIZE ))
MSG="creating  toml-config layers content for $N_WHOLE_PARTITIONS WHOLE paritions (recordset size: $PSIZE) of $GNAME geom"
if [[ REMAINDER -gt 0 ]]; then
    MSG="$MSG with one more remainder-partition (recodset size: $REMAINDER)"
fi
echo partition_geom_layers.sh: $MSG

echo ""
echo "#providers.layers"
if [[ $N_WHOLE_PARTITIONS -gt 0 ]]; then
    for (( index = 1 ; index <= ${N_WHOLE_PARTITIONS} ; index++ )) do
        echo "[[providers.layers]]"
        echo "name = \"${GNAME}_${index}\""
        echo "id_fieldname = \"${IDFIELDNAME}\""
        echo "geometry_fieldname = \"${GEOMFIELDNAME}\""
        SQL="sql = \"SELECT * FROM ${GNAME} l JOIN rtree_${GNAME}_geom si ON l.${IDFIELDNAME} = si.id WHERE l.${IDFIELDNAME}"
        if [[ $index -eq 1 ]]; then
            SQL="$SQL <= $(( $PSIZE * $index ))"
        else
            SQL="$SQL > $(( $PSIZE * ($index - 1) )) AND l.${IDFIELDNAME} <= $(( $PSIZE * $index ))"
        fi
        SQL="$SQL AND ${GEOMFIELDNAME} IS NOT NULL AND !BBOX!\""
        echo "$SQL"
        echo ""
    done
fi
if [[ $REMAINDER -gt 0 ]]; then
    echo "[[providers.layers]]"
    echo "name = \"${GNAME}_${index}\""
    echo "id_fieldname = \"${IDFIELDNAME}\""
    echo "geometry_fieldname = \"${GEOMFIELDNAME}\""
    SQL="sql = \"SELECT * FROM ${GNAME} l JOIN rtree_${GNAME}_geom si ON l.${IDFIELDNAME} = si.id WHERE l.${IDFIELDNAME}"
    SQL="$SQL > $(( $PSIZE * ($index - 1) ))"
    SQL="$SQL AND ${GEOMFIELDNAME} IS NOT NULL AND !BBOX!\""
    echo "$SQL"
    echo ""
fi
echo ""

echo "#maps.layers"
if [[ $N_WHOLE_PARTITIONS -gt 0 ]]; then
    for (( index = 1 ; index <= ${N_WHOLE_PARTITIONS} ; index++ )) do
        echo "[[maps.layers]]"
        echo "name = \"${GNAME}_${index}\""
        echo "provider_layer = \"${PNAME}.${GNAME}_${index}\""
        echo "min_zoom = ${ZMIN}"
        echo "max_zoom = ${ZMAX}"
        echo ""
    done
fi
if [[ $REMAINDER -gt 0 ]]; then
    echo "[[maps.layers]]"
    echo "name = \"${GNAME}_${index}\""
    echo "provider_layer = \"${PNAME}.${GNAME}_${index}\""
    echo "min_zoom = ${ZMIN}"
    echo "max_zoom = ${ZMAX}"
    echo ""
fi
echo ""