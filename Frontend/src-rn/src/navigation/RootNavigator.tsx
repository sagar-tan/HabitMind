import React from 'react';
import { View, Text, StyleSheet, Pressable } from 'react-native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import {
    Home, CalendarCheck, Repeat, BookOpen, BarChart3,
} from 'lucide-react-native';
import { useTheme } from '../theme/ThemeContext';
import { borderRadius, fontSize, spacing } from '../theme';

// Import screens
import { HomeScreen } from '../screens/HomeScreen';
import { PlanScreen } from '../screens/PlanScreen';
import { HabitsScreen } from '../screens/HabitsScreen';
import { JournalScreen } from '../screens/JournalScreen';
import { InsightsScreen } from '../screens/InsightsScreen';
import { SettingsScreen } from '../screens/SettingsScreen';

// Types
export type RootStackParamList = {
    HomeTabs: undefined;
    Settings: undefined;
    WeeklyReview: undefined;
    JournalEntry: undefined;
};

export type TabParamList = {
    Home: undefined;
    Plan: undefined;
    Habits: undefined;
    Journal: undefined;
    Insights: undefined;
};

const Tab = createBottomTabNavigator<TabParamList>();
const Stack = createNativeStackNavigator<RootStackParamList>();

// Custom Tab Bar
function CustomTabBar({ state, descriptors, navigation }: any) {
    const { colors } = useTheme();

    return (
        <View style={[styles.tabBar, { backgroundColor: colors.card, borderTopColor: colors.border }]}>
            {state.routes.map((route: any, index: number) => {
                const { options } = descriptors[route.key];
                const label = options.tabBarLabel ?? options.title ?? route.name;
                const isFocused = state.index === index;

                const icons: Record<string, React.ComponentType<any>> = {
                    Home: Home,
                    Plan: CalendarCheck,
                    Habits: Repeat,
                    Journal: BookOpen,
                    Insights: BarChart3,
                };
                const Icon = icons[route.name] || Home;

                const onPress = () => {
                    const event = navigation.emit({
                        type: 'tabPress',
                        target: route.key,
                        canPreventDefault: true,
                    });

                    if (!isFocused && !event.defaultPrevented) {
                        navigation.navigate(route.name);
                    }
                };

                return (
                    <Pressable
                        key={route.key}
                        onPress={onPress}
                        style={styles.tabItem}
                    >
                        <View
                            style={[
                                styles.tabIconContainer,
                                isFocused && { backgroundColor: `${colors.primary}15` },
                            ]}
                        >
                            <Icon
                                size={20}
                                color={isFocused ? colors.primary : colors.mutedForeground}
                            />
                        </View>
                        <Text
                            style={[
                                styles.tabLabel,
                                { color: isFocused ? colors.primary : colors.mutedForeground },
                            ]}
                        >
                            {label}
                        </Text>
                    </Pressable>
                );
            })}
        </View>
    );
}

// Tab Navigator
function TabNavigator() {
    return (
        <Tab.Navigator
            tabBar={(props) => <CustomTabBar {...props} />}
            screenOptions={{
                headerShown: false,
            }}
        >
            <Tab.Screen name="Home" component={HomeScreen} />
            <Tab.Screen name="Plan" component={PlanScreen} />
            <Tab.Screen name="Habits" component={HabitsScreen} />
            <Tab.Screen name="Journal" component={JournalScreen} />
            <Tab.Screen name="Insights" component={InsightsScreen} />
        </Tab.Navigator>
    );
}

// Root Navigator
export function RootNavigator() {
    const { colors } = useTheme();

    return (
        <Stack.Navigator
            screenOptions={{
                headerShown: false,
                contentStyle: { backgroundColor: colors.background },
            }}
        >
            <Stack.Screen name="HomeTabs" component={TabNavigator} />
            <Stack.Screen
                name="Settings"
                component={SettingsScreen}
                options={{
                    animation: 'slide_from_right',
                }}
            />
        </Stack.Navigator>
    );
}

const styles = StyleSheet.create({
    tabBar: {
        flexDirection: 'row',
        paddingBottom: spacing.xl,
        paddingTop: spacing.md,
        borderTopWidth: 1,
    },
    tabItem: {
        flex: 1,
        alignItems: 'center',
        gap: spacing.xs,
    },
    tabIconContainer: {
        width: 48,
        height: 32,
        borderRadius: borderRadius.lg,
        alignItems: 'center',
        justifyContent: 'center',
    },
    tabLabel: {
        fontSize: fontSize.xs,
    },
});
