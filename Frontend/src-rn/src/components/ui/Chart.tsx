/**
 * PLACEHOLDER: Chart Components
 * 
 * Web version uses recharts
 * 
 * For React Native, implement using one of:
 * - victory-native (recommended - matches recharts API somewhat)
 * - react-native-chart-kit
 * - react-native-svg-charts
 * 
 * Installation:
 * npm install victory-native react-native-svg
 * # or
 * npx expo install victory-native react-native-svg
 * 
 * Example with victory-native:
 * 
 * import { VictoryChart, VictoryLine, VictoryBar, VictoryArea, VictoryAxis } from 'victory-native';
 * 
 * <VictoryChart theme={VictoryTheme.material}>
 *   <VictoryLine data={data} x="week" y="value" />
 * </VictoryChart>
 */

import React from 'react';
import { View, Text, StyleSheet, Dimensions } from 'react-native';
import { useTheme } from '../../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../../theme';

const CHART_HEIGHT = 200;

// Simple Bar Chart placeholder
interface BarChartData {
    name: string;
    value: number;
}

interface SimpleBarChartProps {
    data: BarChartData[];
    height?: number;
}

export function SimpleBarChart({ data, height = CHART_HEIGHT }: SimpleBarChartProps) {
    const { colors } = useTheme();
    const maxValue = Math.max(...data.map((d) => d.value), 1);

    return (
        <View style={[styles.chartContainer, { height }]}>
            <View style={styles.barsContainer}>
                {data.map((item, index) => (
                    <View key={index} style={styles.barWrapper}>
                        <View style={styles.barTrack}>
                            <View
                                style={[
                                    styles.bar,
                                    {
                                        height: `${(item.value / maxValue) * 100}%`,
                                        backgroundColor: colors.primary,
                                    },
                                ]}
                            />
                        </View>
                        <Text style={[styles.barLabel, { color: colors.mutedForeground }]}>{item.name}</Text>
                    </View>
                ))}
            </View>
        </View>
    );
}

// Simple Line Chart placeholder
interface LineChartData {
    name: string;
    value: number;
}

interface SimpleLineChartProps {
    data: LineChartData[];
    height?: number;
}

export function SimpleLineChart({ data, height = CHART_HEIGHT }: SimpleLineChartProps) {
    const { colors } = useTheme();

    // This is a simplified placeholder. For production, use victory-native or similar.
    return (
        <View style={[styles.chartContainer, { height }]}>
            <View style={styles.lineChartPlaceholder}>
                <Text style={[styles.placeholderText, { color: colors.mutedForeground }]}>
                    📈 Line Chart
                </Text>
                <Text style={[styles.placeholderSubtext, { color: colors.mutedForeground }]}>
                    Install victory-native for actual charts
                </Text>
                {data.length > 0 && (
                    <Text style={[styles.dataPreview, { color: colors.foreground }]}>
                        Data: {data.map((d) => d.value).join(' → ')}
                    </Text>
                )}
            </View>
        </View>
    );
}

// Area Chart placeholder
interface AreaChartProps {
    data: LineChartData[];
    height?: number;
}

export function SimpleAreaChart({ data, height = CHART_HEIGHT }: AreaChartProps) {
    const { colors } = useTheme();

    return (
        <View style={[styles.chartContainer, { height }]}>
            <View style={styles.lineChartPlaceholder}>
                <Text style={[styles.placeholderText, { color: colors.mutedForeground }]}>
                    📊 Area Chart
                </Text>
                <Text style={[styles.placeholderSubtext, { color: colors.mutedForeground }]}>
                    Install victory-native for actual charts
                </Text>
            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    chartContainer: {
        width: '100%',
    },
    barsContainer: {
        flex: 1,
        flexDirection: 'row',
        alignItems: 'flex-end',
        gap: spacing.sm,
        paddingBottom: spacing.xl,
    },
    barWrapper: {
        flex: 1,
        alignItems: 'center',
    },
    barTrack: {
        flex: 1,
        width: '80%',
        justifyContent: 'flex-end',
    },
    bar: {
        width: '100%',
        borderRadius: borderRadius.sm,
        minHeight: 4,
    },
    barLabel: {
        fontSize: fontSize.xs,
        marginTop: spacing.xs,
    },
    lineChartPlaceholder: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        borderRadius: borderRadius.lg,
        borderWidth: 1,
        borderColor: 'rgba(128, 128, 128, 0.2)',
        borderStyle: 'dashed',
    },
    placeholderText: {
        fontSize: fontSize.lg,
        marginBottom: spacing.xs,
    },
    placeholderSubtext: {
        fontSize: fontSize.sm,
    },
    dataPreview: {
        fontSize: fontSize.sm,
        marginTop: spacing.md,
    },
});
