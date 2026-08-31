package com.pranay.fleettrack.ui.admin

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pranay.fleettrack.model.DailyLog
import com.pranay.fleettrack.ui.theme.*
import com.pranay.fleettrack.util.toCurrencyString
import com.pranay.fleettrack.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen(
    adminViewModel: AdminViewModel,
    onBack: () -> Unit
) {
    val drivers by adminViewModel.drivers.collectAsStateWithLifecycle()
    val cars by adminViewModel.cars.collectAsStateWithLifecycle()
    val dailyLogs by adminViewModel.dailyLogs.collectAsStateWithLifecycle()

    val monthlyIncome = adminViewModel.monthlyIncome
    val monthlyExpense = adminViewModel.monthlyExpense
    val monthlyProfit = adminViewModel.monthlyProfit

    val aiSummary by adminViewModel.aiSummary.collectAsStateWithLifecycle()
    val isGeneratingSummary by adminViewModel.isGeneratingSummary.collectAsStateWithLifecycle()

    // Trigger animation on composition
    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTriggered = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Statistics",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = SurfaceDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Monthly Overview Card
            MonthlyOverviewCard(
                income = monthlyIncome,
                expense = monthlyExpense,
                profit = monthlyProfit
            )

            // 2. Income vs Expense Bar Chart (last 7 days)
            Last7DaysChart(
                dailyLogs = dailyLogs,
                animationTriggered = animationTriggered
            )

            // 3. Per-Car Performance
            PerCarPerformance(
                dailyLogs = dailyLogs,
                animationTriggered = animationTriggered
            )

            // 4. Per-Driver Comparison
            PerDriverComparison(
                dailyLogs = dailyLogs,
                animationTriggered = animationTriggered
            )

            // 5. AI Summary
            AISummarySection(
                aiSummary = aiSummary,
                isGenerating = isGeneratingSummary,
                onGenerateClick = {
                    adminViewModel.generateAISummary(monthlyIncome, monthlyExpense, monthlyProfit)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MonthlyOverviewCard(
    income: Double,
    expense: Double,
    profit: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Monthly Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Income",
                    value = income.toCurrencyString(),
                    color = IncomeBlue,
                    icon = Icons.Filled.TrendingUp
                )
                StatItem(
                    label = "Expense",
                    value = expense.toCurrencyString(),
                    color = ExpenseAmber,
                    icon = Icons.Filled.TrendingDown
                )
                StatItem(
                    label = "Profit",
                    value = profit.toCurrencyString(),
                    color = ProfitGreen,
                    icon = Icons.Filled.AccountBalance
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun Last7DaysChart(
    dailyLogs: List<DailyLog>,
    animationTriggered: Boolean
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    // Group logs by day for the last 7 days
    val last7Days = remember(dailyLogs) {
        val days = mutableListOf<DayData>()
        val cal = Calendar.getInstance()
        for (i in 6 downTo 0) {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.time

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            val dayEnd = cal.time

            val dayLogs = dailyLogs.filter {
                val logDate = it.date.toDate()
                logDate in dayStart..dayEnd
            }

            days.add(
                DayData(
                    label = dateFormat.format(dayStart),
                    income = dayLogs.sumOf { it.income },
                    expense = dayLogs.sumOf { it.expense }
                )
            )
        }
        days
    }

    val maxValue = remember(last7Days) {
        (last7Days.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0).coerceAtLeast(1.0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Last 7 Days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = IncomeBlue, label = "Income")
                LegendItem(color = ExpenseAmber, label = "Expense")
            }

            // Bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                last7Days.forEach { day ->
                    DayBarGroup(
                        day = day,
                        maxValue = maxValue,
                        animationTriggered = animationTriggered,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

private data class DayData(
    val label: String,
    val income: Double,
    val expense: Double
)

@Composable
private fun DayBarGroup(
    day: DayData,
    maxValue: Double,
    animationTriggered: Boolean,
    modifier: Modifier = Modifier
) {
    val incomeRatio = (day.income / maxValue).toFloat().coerceIn(0f, 1f)
    val expenseRatio = (day.expense / maxValue).toFloat().coerceIn(0f, 1f)

    val animatedIncome by animateFloatAsState(
        targetValue = if (animationTriggered) incomeRatio else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 200),
        label = "income"
    )
    val animatedExpense by animateFloatAsState(
        targetValue = if (animationTriggered) expenseRatio else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "expense"
    )

    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Income bar
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight(fraction = animatedIncome.coerceAtLeast(0.02f))
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(listOf(Blue400, Blue600))
                    )
            )
            // Expense bar
            Box(
                modifier = Modifier
                    .width(12.dp)
                    .fillMaxHeight(fraction = animatedExpense.coerceAtLeast(0.02f))
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(
                        Brush.verticalGradient(listOf(Amber400, Amber600))
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = day.label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PerCarPerformance(
    dailyLogs: List<DailyLog>,
    animationTriggered: Boolean
) {
    val carStats = remember(dailyLogs) {
        dailyLogs.groupBy { it.carName }.map { (carName, logs) ->
            EntityStat(
                name = carName,
                income = logs.sumOf { it.income },
                expense = logs.sumOf { it.expense }
            )
        }.sortedByDescending { it.income - it.expense }
    }

    if (carStats.isEmpty()) return

    val maxValue = remember(carStats) {
        carStats.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = null,
                    tint = Blue400,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Per-Car Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            carStats.forEach { stat ->
                HorizontalBarRow(
                    stat = stat,
                    maxValue = maxValue,
                    animationTriggered = animationTriggered
                )
            }
        }
    }
}

@Composable
private fun PerDriverComparison(
    dailyLogs: List<DailyLog>,
    animationTriggered: Boolean
) {
    val driverStats = remember(dailyLogs) {
        dailyLogs.groupBy { it.driverName }.map { (driverName, logs) ->
            EntityStat(
                name = driverName,
                income = logs.sumOf { it.income },
                expense = logs.sumOf { it.expense }
            )
        }.sortedByDescending { it.income - it.expense }
    }

    if (driverStats.isEmpty()) return

    val maxValue = remember(driverStats) {
        driverStats.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = null,
                    tint = Emerald400,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Per-Driver Comparison",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            driverStats.forEach { stat ->
                HorizontalBarRow(
                    stat = stat,
                    maxValue = maxValue,
                    animationTriggered = animationTriggered
                )
            }
        }
    }
}

private data class EntityStat(
    val name: String,
    val income: Double,
    val expense: Double
)

@Composable
private fun HorizontalBarRow(
    stat: EntityStat,
    maxValue: Double,
    animationTriggered: Boolean
) {
    val incomeRatio = (stat.income / maxValue).toFloat().coerceIn(0f, 1f)
    val expenseRatio = (stat.expense / maxValue).toFloat().coerceIn(0f, 1f)
    val net = stat.income - stat.expense

    val animatedIncome by animateFloatAsState(
        targetValue = if (animationTriggered) incomeRatio else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "income"
    )
    val animatedExpense by animateFloatAsState(
        targetValue = if (animationTriggered) expenseRatio else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 150),
        label = "expense"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stat.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = if (net >= 0) "+${net.toCurrencyString()}" else net.toCurrencyString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (net >= 0) Emerald400 else Rose400
            )
        }

        // Income bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceCardLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedIncome.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Blue600, Blue400))
                    )
            )
        }

        // Expense bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceCardLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = animatedExpense.coerceAtLeast(0.01f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(listOf(Amber600, Amber400))
                    )
            )
        }
    }
}
